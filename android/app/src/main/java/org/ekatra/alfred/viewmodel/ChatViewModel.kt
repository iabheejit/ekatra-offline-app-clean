package org.ekatra.alfred.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.ekatra.alfred.InferenceEngine
import org.ekatra.alfred.data.AppSettings
import org.ekatra.alfred.data.RemoteConfigManager
import org.ekatra.alfred.data.model.ChatMessageEntity
import org.ekatra.alfred.data.model.ChatSession
import org.ekatra.alfred.data.repository.ChatRepository
import org.ekatra.alfred.data.repository.SavedContentRepository
import org.ekatra.alfred.notification.AppUpdateChecker
import org.ekatra.alfred.tools.MathInterceptor
import org.ekatra.alfred.tools.PromptFormatter
import java.io.File
import javax.inject.Inject

data class ChatUiState(
    val isLoading: Boolean = true,
    val isGenerating: Boolean = false,
    val modelReady: Boolean = false,
    val statusText: String = "Loading model...",
    val currentSessionId: Long? = null,
    val selectedSubject: String? = null,
    val inputText: String = "",
    val streamingText: String? = null, // null = not streaming
    val serverRunning: Boolean = false,
    val updateInfo: AppUpdateChecker.UpdateInfo? = null,
    val updateDismissed: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val savedContentRepository: SavedContentRepository,
    private val engine: InferenceEngine,
    private val settings: AppSettings,
    private val analyticsHelper: AnalyticsHelper,
    private val remoteConfig: RemoteConfigManager,
    private val appUpdateChecker: AppUpdateChecker
) : ViewModel() {

    companion object {
        private const val TAG = "ChatViewModel"
    }

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        checkForAppUpdate()
    }

    private fun checkForAppUpdate() {
        viewModelScope.launch {
            try {
                val updateInfo = appUpdateChecker.checkForUpdate()
                if (updateInfo.isUpdateAvailable) {
                    _uiState.update { it.copy(updateInfo = updateInfo) }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Update check failed", e)
            }
        }
    }

    fun dismissUpdateBanner() {
        _uiState.update { it.copy(updateDismissed = true) }
    }

    val recentSessions: Flow<List<ChatSession>> = chatRepository.getRecentSessions()

    // Messages for the current session — switches when currentSessionId changes
    val currentMessages: Flow<List<ChatMessageEntity>> = _uiState
        .map { it.currentSessionId }
        .distinctUntilChanged()
        .flatMapLatest { sessionId ->
            if (sessionId != null) chatRepository.getMessagesForSession(sessionId)
            else flowOf(emptyList())
        }

    fun loadModel(modelFile: File) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, statusText = "Loading model...") }
            val success = engine.loadModel(modelFile)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    modelReady = success,
                    statusText = if (success) "Ready" else "Model load failed"
                )
            }
            if (success) {
                analyticsHelper.setModelProperty(settings.getSelectedModel().id)
            }
        }
    }

    fun createInitialSession() {
        if (_uiState.value.currentSessionId != null) return
        viewModelScope.launch {
            val id = chatRepository.createSession(_uiState.value.selectedSubject)
            _uiState.update { it.copy(currentSessionId = id) }
        }
    }

    fun createNewSession() {
        viewModelScope.launch {
            val id = chatRepository.createSession(_uiState.value.selectedSubject)
            _uiState.update { it.copy(currentSessionId = id, streamingText = null, inputText = "") }
        }
    }

    fun switchSession(sessionId: Long, subject: String?) {
        _uiState.update {
            it.copy(currentSessionId = sessionId, selectedSubject = subject, streamingText = null, inputText = "")
        }
    }

    fun updateInput(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun selectSubject(subject: String) {
        val current = _uiState.value.selectedSubject
        _uiState.update { it.copy(selectedSubject = if (current == subject) null else subject) }
    }

    private val mathInterceptor = MathInterceptor()
    private val promptFormatter = PromptFormatter()

    fun sendMessage(userMsg: String) {
        val state = _uiState.value
        if (userMsg.isBlank() || !state.modelReady || state.isGenerating) return

        _uiState.update { it.copy(inputText = "", isGenerating = true) }

        viewModelScope.launch {
            val sessionId = state.currentSessionId ?: chatRepository.createSession(state.selectedSubject).also { id ->
                _uiState.update { it.copy(currentSessionId = id) }
            }

            // Build context using remote config for context window size
            val maxMessages = remoteConfig.maxContextMessages
            val history = chatRepository.getConversationContext(sessionId, maxMessages = maxMessages)
            val systemPrompt = remoteConfig.systemPrompt

            // Calculator intercept: if the message is a math expression,
            // compute the correct answer and ask the model to explain it
            val mathResult = mathInterceptor.intercept(userMsg)
            val effectiveUserMsg = mathResult?.augmentedPrompt ?: "Student: $userMsg"

            val body = buildString {
                if (history.isNotBlank()) {
                    appendLine("Conversation history:")
                    appendLine(history)
                    appendLine()
                }
                appendLine(effectiveUserMsg)
                append("Maya: ")
            }

            // Format prompt with correct model-specific chat-ML template
            val modelId = settings.getSelectedModel().id
            val promptText = promptFormatter.format(modelId, systemPrompt, body)

            // Persist user message
            chatRepository.addMessage(sessionId, userMsg, true)

            // Log analytics
            analyticsHelper.logMessageSent(state.selectedSubject, userMsg.length)

            // Stream AI response
            val startTime = System.currentTimeMillis()
            val response = StringBuilder()
            _uiState.update { it.copy(streamingText = "") }

            // Clear KV cache before each generation to prevent double-fill
            // (history is already re-sent in the prompt text above)
            engine.clearContext()

            engine.generateRaw(promptText).collect { token ->
                response.append(token)
                _uiState.update { it.copy(streamingText = response.toString()) }
            }

            _uiState.update { it.copy(streamingText = null) }

            // Persist AI answer
            chatRepository.addMessage(sessionId, response.toString(), false)

            val elapsed = System.currentTimeMillis() - startTime
            analyticsHelper.logResponseReceived(elapsed, response.length, settings.getSelectedModel().id)

            Log.d(TAG, "AI response complete session=$sessionId chars=${response.length} time=${elapsed}ms")
            _uiState.update { it.copy(isGenerating = false) }
        }
    }

    fun rateMessage(messageId: Long, rating: Int) {
        viewModelScope.launch {
            chatRepository.rateMessage(messageId, rating)
            val ratingStr = if (rating > 0) "up" else "down"
            val sessionId = _uiState.value.currentSessionId?.toString() ?: "unknown"
            analyticsHelper.logResponseRated(ratingStr, sessionId)
        }
    }

    fun saveAnswer(question: String, answer: String, subject: String?) {
        viewModelScope.launch {
            savedContentRepository.saveAnswer(question, answer, subject)
            analyticsHelper.logAnswerSaved(subject)
        }
    }

    fun shareAnswer(subject: String?) {
        analyticsHelper.logAnswerShared(subject, null)
    }

    fun toggleServer(enable: Boolean) {
        _uiState.update { it.copy(serverRunning = enable) }
    }

    fun updateStatusForServer(ip: String) {
        val state = _uiState.value
        if (state.serverRunning && state.modelReady) {
            _uiState.update { it.copy(statusText = "Server at http://$ip:8080") }
        }
    }
}
