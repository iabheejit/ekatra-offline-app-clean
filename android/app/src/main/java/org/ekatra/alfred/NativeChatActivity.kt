package org.ekatra.alfred

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.systemBarsPadding
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import java.util.Locale
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.runtime.rememberUpdatedState
import java.io.File
import java.net.NetworkInterface
import androidx.compose.runtime.Stable
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import org.ekatra.alfred.data.AppSettings
import org.ekatra.alfred.data.sync.SyncWorker
import org.ekatra.alfred.notification.AppUpdateChecker
import org.ekatra.alfred.viewmodel.ChatViewModel
import org.ekatra.alfred.viewmodel.ChatUiState

private const val CHAT_TAG = "NativeChat"

/**
 * NativeChatActivity - Pure Compose UI for faster performance
 * 
 * No WebView overhead - direct Kotlin UI updates.
 */
@AndroidEntryPoint
class NativeChatActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "NativeChatActivity"
    }
    
    @Inject lateinit var settings: AppSettings

    // Text-to-Speech engine — uses system voice settings
    var tts: TextToSpeech? = null
        private set

    private val modelPath: File? by lazy {
        // If the currently selected model file is missing, try to recover by detecting any downloaded model
        val selectedModel = settings.getSelectedModel()
        val directFile = File(filesDir, "models/${selectedModel.filename}")
        if (directFile.exists()) return@lazy directFile

        val recovered = settings.recoverDownloadedModel(this)
        if (recovered != null) {
            Log.w(TAG, "Selected model missing; switched to downloaded model ${recovered.filename}")
            return@lazy File(filesDir, "models/${recovered.filename}")
        }

        null
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Initialize TTS engine (uses whatever voice/speed the user set in system TTS settings)
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.getDefault()
                Log.d(TAG, "TTS initialized: ${Locale.getDefault()}")
            } else {
                Log.w(TAG, "TTS initialization failed with status: $status")
            }
        }

        if (modelPath == null) {
            Log.e(TAG, "Model missing; redirecting to download")
            startActivity(Intent(this, ModelDownloadActivity::class.java))
            finish()
            return
        }

        setContent {
            EkatraChatTheme {
                ChatScreen(
                    modelFile = modelPath!!,
                    onSettingsClick = {
                        startActivity(Intent(this, SettingsActivity::class.java))
                    },
                    onHotspotToggle = { enable ->
                        if (enable) {
                            startService(Intent(this, AlfredServerService::class.java))
                            Log.d(TAG, "Hotspot/server toggled on")
                        } else {
                            stopService(Intent(this, AlfredServerService::class.java))
                            Log.d(TAG, "Hotspot/server toggled off")
                        }
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Trigger immediate sync when app comes to foreground
        SyncWorker.triggerImmediateSync(this)
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        super.onDestroy()
    }
}


@Composable
fun MessageActions(
    alignStart: Boolean,
    onSave: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    Row(
        modifier = Modifier.padding(start = if (alignStart) 40.dp else 0.dp, top = 6.dp, end = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ActionChip(label = "Save", onClick = onSave)
        ActionChip(label = "Copy", onClick = onCopy)
        ActionChip(label = "Share", onClick = onShare)
    }
}

@Composable
fun ActionChip(label: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF1F5F9),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF475569),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

// ==================== Update Banner ====================

@Composable
fun UpdateBanner(
    updateInfo: AppUpdateChecker.UpdateInfo,
    onDismiss: () -> Unit,
    onUpdate: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (updateInfo.isForceUpdate) Color(0xFFFEE2E2) else Color(0xFFEFF6FF),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (updateInfo.isForceUpdate) "⚠️" else "🆕",
                fontSize = 20.sp
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (updateInfo.isForceUpdate) "Update Required" else "Update Available",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = if (updateInfo.isForceUpdate) Color(0xFFDC2626) else Color(0xFF1E40AF)
                )
                Text(
                    text = "Version ${updateInfo.requiredVersion} is available (you have ${updateInfo.currentVersion})",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
            }
            Spacer(Modifier.width(8.dp))
            Surface(
                modifier = Modifier.clickable(onClick = onUpdate),
                shape = RoundedCornerShape(8.dp),
                color = if (updateInfo.isForceUpdate) Color(0xFFDC2626) else Color(0xFF2563EB)
            ) {
                Text(
                    "Update",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
            if (!updateInfo.isForceUpdate) {
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Text("✕", fontSize = 16.sp, color = Color(0xFF9CA3AF))
                }
            }
        }
    }
}

// ==================== Theme ====================

@Composable
fun EkatraChatTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF2563EB),
            onPrimary = Color.White,
            surface = Color(0xFFF0F4F8),
            background = Color(0xFFF0F4F8),
        ),
        content = content
    )
}

// ==================== Data Classes ====================

@Stable
data class ChatMessage(
    val id: Long = System.currentTimeMillis(),
    val text: String,
    val isUser: Boolean,
    val isStreaming: Boolean = false,
    val relatedQuestion: String? = null,
    val subject: String? = null,
    val rating: Int = 0  // 0 = unrated, 1 = thumbs up, -1 = thumbs down
)

// Example questions - defined once to avoid recreation
private val EXAMPLE_QUESTIONS = listOf(
    "Explain photosynthesis simply",
    "What is Newton's first law?",
    "Help me understand fractions",
    "How does the water cycle work?"
)

// Subject quick-start chips
private val SUBJECT_SUGGESTIONS = listOf(
    "Math",
    "Science",
    "English",
    "History",
    "Geography",
    "Coding"
)

// ==================== Main Chat Screen ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    modelFile: File,
    onSettingsClick: () -> Unit,
    onHotspotToggle: (Boolean) -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // Sessions and messages backed by Room flows via ViewModel
    val recentSessions by viewModel.recentSessions.collectAsState(initial = emptyList())
    val sessionMessagesEntities by viewModel.currentMessages.collectAsState(initial = emptyList())

    val persistedMessages = remember(sessionMessagesEntities) {
        sessionMessagesEntities.map {
            ChatMessage(
                id = it.id,
                text = it.text,
                isUser = it.isUser,
                isStreaming = false,
                rating = it.rating
            )
        }
    }

    val messages = remember(persistedMessages, uiState.streamingText) {
        if (uiState.streamingText != null) {
            persistedMessages + ChatMessage(
                id = System.currentTimeMillis(),
                text = uiState.streamingText!!,
                isUser = false,
                isStreaming = true
            )
        } else persistedMessages
    }

    val saveAnswer: (ChatMessage) -> Unit = lambda@ { message ->
        if (message.isUser || message.isStreaming || message.text.isBlank()) return@lambda
        val question = message.relatedQuestion
        if (question.isNullOrBlank()) {
            Toast.makeText(context, "Missing question to save", Toast.LENGTH_SHORT).show()
            return@lambda
        }
        viewModel.saveAnswer(question, message.text, message.subject)
        Toast.makeText(context, "Saved offline", Toast.LENGTH_SHORT).show()
    }

    val copyText: (String) -> Unit = lambda@ { text ->
        if (text.isBlank()) return@lambda
        clipboardManager.setPrimaryClip(ClipData.newPlainText("maya_answer", text))
        Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
    }

    val shareAnswer: (ChatMessage) -> Unit = lambda@ { message ->
        if (message.text.isBlank() || message.isStreaming) return@lambda
        viewModel.shareAnswer(message.subject)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message.text)
            putExtra(Intent.EXTRA_SUBJECT, message.relatedQuestion ?: "Maya AI answer")
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share answer"))
    }

    val rateMessage: (Long, Int) -> Unit = { messageId, rating ->
        viewModel.rateMessage(messageId, rating)
    }

    val speakText: (String) -> Unit = lambda@ { text ->
        if (text.isBlank()) return@lambda
        val activity = context as? NativeChatActivity ?: return@lambda
        val engine = activity.tts ?: return@lambda
        if (engine.isSpeaking) {
            engine.stop()
        } else {
            engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "maya_${System.currentTimeMillis()}")
        }
    }
    
    // Combined effects for status updates
    LaunchedEffect(uiState.serverRunning, uiState.modelReady, uiState.isLoading) {
        if (uiState.serverRunning && uiState.modelReady) {
            val ip = getWifiHotspotIpAddress(context)
            viewModel.updateStatusForServer(ip)
        }
    }
    
    // Load model and create initial session
    LaunchedEffect(Unit) {
        viewModel.createInitialSession()
        viewModel.loadModel(modelFile)
    }
    
    // Auto-scroll when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Chats",
                        modifier = Modifier.padding(16.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    // New Chat item
                    NavigationDrawerItem(
                        label = { Text("➕ New chat") },
                        selected = false,
                        onClick = {
                            scope.launch {
                                viewModel.createNewSession()
                                drawerState.close()
                                Toast.makeText(context, "New chat created", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = NavigationDrawerItemDefaults.colors()
                    )

                    HorizontalDivider()

                    if (recentSessions.isEmpty()) {
                        Text(
                            text = "No chats yet",
                            modifier = Modifier.padding(16.dp),
                            color = Color(0xFF6B7280)
                        )
                    } else {
                        recentSessions.forEach { session ->
                            NavigationDrawerItem(
                                label = {
                                    Column {
                                        Text(session.title.ifBlank { "Untitled chat" })
                                        session.subject?.let {
                                            Text(
                                                it,
                                                fontSize = 12.sp,
                                                color = Color(0xFF6B7280)
                                            )
                                        }
                                    }
                                },
                                selected = session.id == uiState.currentSessionId,
                                onClick = {
                                    viewModel.switchSession(session.id, session.subject)
                                    scope.launch { drawerState.close() }
                                }
                            )
                        }
                    }
                }
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF0F4F8))
                .systemBarsPadding()
        ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFF2563EB), Color(0xFF1E40AF))
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Text("☰", fontSize = 20.sp, color = Color.White)
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Avatar
                    Image(
                        painter = painterResource(id = R.drawable.maya_ai),
                        contentDescription = "Maya AI avatar",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Text(
                            text = "Maya AI",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = if (uiState.isGenerating) "Typing..." else uiState.statusText,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }
                }
                
                // Action Buttons Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Hotspot Info/Toggle
                    IconButton(
                        onClick = { 
                            if (!uiState.serverRunning) {
                                viewModel.toggleServer(true)
                                onHotspotToggle(true)
                                val ip = getWifiHotspotIpAddress(context)
                                Toast.makeText(
                                    context,
                                    "Server started!\nConnect others to: http://$ip:8080",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                viewModel.toggleServer(false)
                                onHotspotToggle(false)
                                Toast.makeText(context, "Server stopped", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Text(
                            if (uiState.serverRunning) "📡" else "📴",
                            fontSize = 20.sp
                        )
                    }
                    
                    // Settings Button
                    IconButton(onClick = onSettingsClick) {
                        Text("⚙️", fontSize = 20.sp)
                    }
                }
            }
        }
        
        // Update available banner
        val updateInfo = uiState.updateInfo
        if (updateInfo != null && updateInfo.isUpdateAvailable && !uiState.updateDismissed) {
            UpdateBanner(
                updateInfo = updateInfo,
                onDismiss = { viewModel.dismissUpdateBanner() },
                onUpdate = {
                    // Open Play Store or download page
                    try {
                        val intent = Intent(Intent.ACTION_VIEW,
                            android.net.Uri.parse("market://details?id=${context.packageName}"))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        val intent = Intent(Intent.ACTION_VIEW,
                            android.net.Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}"))
                        context.startActivity(intent)
                    }
                }
            )
        }

        // Maintenance message banner
        // (RemoteConfigManager.maintenanceMessage can be set from Firebase Console)

        // Loading overlay
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF0F4F8)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Simple loading dots instead of CircularProgressIndicator
                    Text(
                        "⏳",
                        fontSize = 48.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(uiState.statusText, color = Color(0xFF64748B))
                }
            }
        } else {
            SubjectChipsRow(
                selectedSubject = uiState.selectedSubject,
                onSubjectSelected = { subject ->
                    Log.d(CHAT_TAG, "Subject chip tapped: $subject")
                    viewModel.selectSubject(subject)
                    Log.d(CHAT_TAG, "Subject now ${uiState.selectedSubject ?: "none"}")
                }
            )

            // Chat messages
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Welcome message if empty
                if (messages.isEmpty()) {
                    item {
                        WelcomeCard(
                            onQuestionClick = { question ->
                                if (uiState.modelReady && !uiState.isGenerating) {
                                    viewModel.sendMessage(question)
                                }
                            }
                        )
                    }
                }
                
                items(messages, key = { it.id }) { message ->
                    MessageBubble(
                        message = message,
                        onSave = saveAnswer,
                        onCopy = copyText,
                        onShare = shareAnswer,
                        onRate = rateMessage,
                        onSpeak = speakText
                    )
                }
            }
            
            // Input area
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.inputText,
                        onValueChange = { viewModel.updateInput(it) },
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(
                                if (uiState.selectedSubject != null) "Ask about ${uiState.selectedSubject}..." else "Ask Maya mam anything..."
                            )
                        },
                        enabled = uiState.modelReady && !uiState.isGenerating,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2563EB),
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (uiState.inputText.isNotBlank() && uiState.modelReady && !uiState.isGenerating) {
                                    val userMsg = uiState.inputText.trim()
                                    viewModel.updateInput("")
                                    focusManager.clearFocus()
                                    viewModel.sendMessage(userMsg)
                                }
                            }
                        ),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            if (uiState.inputText.isNotBlank() && uiState.modelReady && !uiState.isGenerating) {
                                val userMsg = uiState.inputText.trim()
                                viewModel.updateInput("")
                                focusManager.clearFocus()
                                viewModel.sendMessage(userMsg)
                            }
                        },
                        enabled = uiState.modelReady && !uiState.isGenerating && uiState.inputText.isNotBlank(),
                        shape = CircleShape,
                        contentPadding = PaddingValues(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2563EB)
                        )
                    ) {
                        Text("→", fontSize = 20.sp)
                    }
                }
            }
        }
    }
}

}

// ==================== Components ====================

@Composable
fun WelcomeCard(onQuestionClick: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.maya_ai),
                contentDescription = "Maya AI",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Namaste! I'm Maya mam",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF1E293B)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your AI teacher. Ask me anything!",
                color = Color(0xFF64748B),
                fontSize = 14.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Example Questions
            EXAMPLE_QUESTIONS.forEach { question ->
                QuestionChip(
                    text = question,
                    onClick = { onQuestionClick(question) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun QuestionChip(text: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF0F4F8),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "💡",
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                fontSize = 14.sp,
                color = Color(0xFF475569)
            )
        }
    }
}

@Composable
fun SubjectChipsRow(
    selectedSubject: String?,
    onSubjectSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SUBJECT_SUGGESTIONS.forEach { subject ->
            val isSelected = selectedSubject == subject
            Surface(
                modifier = Modifier.clickable { onSubjectSelected(subject) },
                shape = RoundedCornerShape(50),
                color = if (isSelected) Color(0xFFDBEAFE) else Color(0xFFF8FAFC),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) Color(0xFF2563EB) else Color(0xFFE2E8F0)
                )
            ) {
                Text(
                    text = subject,
                    color = if (isSelected) Color(0xFF1E3A8A) else Color(0xFF475569),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: ChatMessage,
    onSave: (ChatMessage) -> Unit,
    onCopy: (String) -> Unit,
    onShare: (ChatMessage) -> Unit,
    onRate: (Long, Int) -> Unit = { _, _ -> },
    onSpeak: (String) -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
        ) {
            if (!message.isUser) {
                Image(
                    painter = painterResource(id = R.drawable.maya_ai),
                    contentDescription = "Maya AI",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (message.isUser) 16.dp else 4.dp,
                    bottomEnd = if (message.isUser) 4.dp else 16.dp
                ),
                color = if (message.isUser) Color(0xFF2563EB) else Color.White,
                shadowElevation = if (message.isUser) 0.dp else 2.dp,
                modifier = Modifier.fillMaxWidth(0.75f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message.text.ifEmpty { if (message.isStreaming) "..." else "" },
                        color = if (message.isUser) Color.White else Color(0xFF1E293B),
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )

                    if (message.isStreaming && message.text.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "▌",
                            color = Color(0xFF2563EB),
                            fontSize = 15.sp
                        )
                    }
                }
            }

            if (message.isUser) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF22C55E)),
                    contentAlignment = Alignment.Center
                ) {
                }
            }
        }

        if (!message.isUser && !message.isStreaming && message.text.isNotBlank()) {
            // Thumbs up/down rating
            Row(
                modifier = Modifier.padding(start = 40.dp, top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = { onRate(message.id, if (message.rating == 1) 0 else 1) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Text(
                        "👍",
                        fontSize = 14.sp,
                        color = if (message.rating == 1) Color(0xFF2563EB) else Color(0xFF9CA3AF)
                    )
                }
                IconButton(
                    onClick = { onRate(message.id, if (message.rating == -1) 0 else -1) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Text(
                        "👎",
                        fontSize = 14.sp,
                        color = if (message.rating == -1) Color(0xFFEF4444) else Color(0xFF9CA3AF)
                    )
                }
                // Read aloud button
                IconButton(
                    onClick = { onSpeak(message.text) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Text("🔊", fontSize = 14.sp)
                }
            }

            MessageActions(
                alignStart = true,
                onSave = { onSave(message) },
                onCopy = { onCopy(message.text) },
                onShare = { onShare(message) }
            )
        }
    }
}

// ==================== Helper Functions ====================

/**
 * Get WiFi IP address for hotspot sharing
 */
@Suppress("UNUSED_PARAMETER")
fun getWifiHotspotIpAddress(context: android.content.Context): String {
    try {
        val interfaces = NetworkInterface.getNetworkInterfaces()
        while (interfaces.hasMoreElements()) {
            val intf = interfaces.nextElement()
            // Check for wlan0 (WiFi) or ap0 (hotspot) interfaces
            if (intf.name.startsWith("wlan") || intf.name.startsWith("ap")) {
                val addrs = intf.inetAddresses
                while (addrs.hasMoreElements()) {
                    val addr = addrs.nextElement()
                    if (!addr.isLoopbackAddress && addr.address.size == 4) {
                        return addr.hostAddress ?: "192.168.43.1"
                    }
                }
            }
        }
    } catch (e: Exception) {
        Log.e("NativeChatActivity", "Failed to get IP", e)
    }
    return "192.168.43.1" // Default hotspot IP
}
