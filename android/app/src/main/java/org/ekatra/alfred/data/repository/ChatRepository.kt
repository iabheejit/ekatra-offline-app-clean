package org.ekatra.alfred.data.repository

import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import org.ekatra.alfred.data.local.ChatMessageDao
import org.ekatra.alfred.data.local.ChatSessionDao
import org.ekatra.alfred.data.model.ChatSession
import org.ekatra.alfred.data.model.ChatMessageEntity

/**
 * Repository for managing chat sessions and messages.
 * Injected via Hilt (see DatabaseModule).
 */
class ChatRepository(
    private val sessionDao: ChatSessionDao,
    private val messageDao: ChatMessageDao
) {
    private val gson = Gson()

    // Session operations
    fun getAllSessions(): Flow<List<ChatSession>> = sessionDao.getAllSessions()
    
    fun getRecentSessions(): Flow<List<ChatSession>> = sessionDao.getRecentSessions()
    
    suspend fun getSession(sessionId: Long): ChatSession? = sessionDao.getSession(sessionId)
    
    suspend fun createSession(subject: String? = null, userId: String? = null): Long {
        val session = ChatSession(
            title = "New Chat",
            subject = subject,
            userId = userId
        )
        return sessionDao.insert(session)
    }
    
    suspend fun updateSessionTitle(sessionId: Long, title: String) {
        sessionDao.updateTitle(sessionId, title)
    }
    
    suspend fun deleteSession(sessionId: Long) {
        sessionDao.deleteById(sessionId)
    }
    
    suspend fun deleteAllSessions() {
        sessionDao.deleteAll()
    }
    
    // Message operations
    fun getMessagesForSession(sessionId: Long): Flow<List<ChatMessageEntity>> {
        return messageDao.getMessagesForSession(sessionId)
    }
    
    suspend fun getMessagesForSessionSync(sessionId: Long): List<ChatMessageEntity> {
        return messageDao.getMessagesForSessionSync(sessionId)
    }
    
    suspend fun getRecentMessages(sessionId: Long, limit: Int = 10): List<ChatMessageEntity> {
        return messageDao.getRecentMessages(sessionId, limit)
    }
    
    suspend fun addMessage(sessionId: Long, text: String, isUser: Boolean, userId: String? = null): Long {
        val message = ChatMessageEntity(
            sessionId = sessionId,
            text = text,
            isUser = isUser,
            userId = userId
        )
        val messageId = messageDao.insert(message)
        
        // Update session timestamp and count
        sessionDao.updateSessionTimestamp(sessionId)
        
        // If this is the first user message, use it as the title
        if (isUser) {
            val session = sessionDao.getSession(sessionId)
            if (session?.title == "New Chat") {
                val title = text.take(40) + if (text.length > 40) "..." else ""
                sessionDao.updateTitle(sessionId, title)
            }
        }
        
        return messageId
    }

    suspend fun rateMessage(messageId: Long, rating: Int) {
        messageDao.updateRating(messageId, rating)
    }
    
    /**
     * Get conversation history formatted for LLM context
     */
    suspend fun getConversationContext(sessionId: Long, maxMessages: Int = 6): String {
        val messages = messageDao.getRecentMessages(sessionId, maxMessages).reversed()
        if (messages.isEmpty()) return ""
        
        return messages.joinToString("\n") { msg ->
            if (msg.isUser) "Student: ${msg.text}" else "Maya: ${msg.text}"
        }
    }

    /**
     * Export all chat data as JSON for privacy compliance
     */
    suspend fun exportAllAsJson(): String {
        val sessions = sessionDao.getAllSessionsSync()
        val data = sessions.map { session ->
            val messages = messageDao.getMessagesForSessionSync(session.id)
            mapOf(
                "session" to session,
                "messages" to messages
            )
        }
        return gson.toJson(data)
    }
}
