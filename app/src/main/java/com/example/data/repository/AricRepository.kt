package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.AricDao
import com.example.data.local.ChatMessageEntity
import com.example.data.local.MemoryEntity
import com.example.data.local.RoutineEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AricSettings(
    val assistantName: String = "ARIC",
    val ownerName: String = "Boss",
    val language: String = "Hinglish", // Hinglish, Hindi, English
    val speechRate: Float = 1.0f,
    val speechPitch: Float = 1.0f,
    val customSystemPrompt: String = "You are ARIC, an elite private AI assistant operating with full control on your Boss's phone. Always address the user as 'Boss'. Respond smartly, crisply, and helpfully in Hindi/Hinglish or English. Confirm actions with 'Yes Boss', 'Right away Boss', or 'Samajh gaya Boss'.",
    val apiKey: String = ""
)

class AricRepository(
    private val dao: AricDao,
    private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("aric_settings_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings = _settings.asStateFlow()

    // Memories
    val allMemories: Flow<List<MemoryEntity>> = dao.getAllMemories()

    fun searchMemories(query: String): Flow<List<MemoryEntity>> = dao.searchMemories(query)

    suspend fun addMemory(text: String, category: String = "Personal"): Long {
        return dao.insertMemory(MemoryEntity(text = text.trim(), category = category))
    }

    suspend fun deleteMemory(memory: MemoryEntity) {
        dao.deleteMemory(memory)
    }

    suspend fun deleteMemoryById(id: Long) {
        dao.deleteMemoryById(id)
    }

    suspend fun deleteLastMemory(): MemoryEntity? {
        val last = dao.getLastMemory()
        if (last != null) {
            dao.deleteMemory(last)
        }
        return last
    }

    suspend fun clearAllMemories() {
        dao.clearAllMemories()
    }

    // Routines
    val allRoutines: Flow<List<RoutineEntity>> = dao.getAllRoutines()
    val enabledRoutines: Flow<List<RoutineEntity>> = dao.getEnabledRoutines()

    suspend fun getEnabledRoutinesList(): List<RoutineEntity> {
        return dao.getEnabledRoutinesList()
    }

    suspend fun saveRoutine(routine: RoutineEntity): Long {
        return dao.insertRoutine(routine)
    }

    suspend fun toggleRoutine(routine: RoutineEntity, isEnabled: Boolean) {
        dao.updateRoutine(routine.copy(isEnabled = isEnabled))
    }

    suspend fun deleteRoutine(routine: RoutineEntity) {
        dao.deleteRoutine(routine)
    }

    suspend fun deleteRoutineById(id: Long) {
        dao.deleteRoutineById(id)
    }

    // Chat
    val chatHistory: Flow<List<ChatMessageEntity>> = dao.getRecentChat()

    suspend fun addChatMessage(sender: String, message: String, actionTaken: String? = null): Long {
        return dao.insertChat(
            ChatMessageEntity(
                sender = sender,
                message = message,
                actionTaken = actionTaken
            )
        )
    }

    suspend fun clearChat() {
        dao.clearChat()
    }

    // Settings
    private fun loadSettings(): AricSettings {
        return AricSettings(
            assistantName = prefs.getString("assistant_name", "ARIC") ?: "ARIC",
            ownerName = prefs.getString("owner_name", "Boss") ?: "Boss",
            language = prefs.getString("language", "Hinglish") ?: "Hinglish",
            speechRate = prefs.getFloat("speech_rate", 1.0f),
            speechPitch = prefs.getFloat("speech_pitch", 1.0f),
            customSystemPrompt = prefs.getString(
                "custom_system_prompt",
                "You are ARIC, a private personal AI assistant running on the owner's Android phone. Speak naturally, briefly, and helpfully in conversational Hindi/Hinglish or English."
            ) ?: "",
            apiKey = prefs.getString("custom_api_key", "") ?: ""
        )
    }

    fun updateSettings(newSettings: AricSettings) {
        prefs.edit()
            .putString("assistant_name", newSettings.assistantName)
            .putString("owner_name", newSettings.ownerName)
            .putString("language", newSettings.language)
            .putFloat("speech_rate", newSettings.speechRate)
            .putFloat("speech_pitch", newSettings.speechPitch)
            .putString("custom_system_prompt", newSettings.customSystemPrompt)
            .putString("custom_api_key", newSettings.apiKey)
            .apply()
        _settings.value = newSettings
    }
}
