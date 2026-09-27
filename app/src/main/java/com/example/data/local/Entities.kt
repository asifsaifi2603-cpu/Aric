package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val category: String = "Personal",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val triggerPhrase: String,
    val actionType: String, // SPEAK, OPEN_APP, OPEN_URL, TOGGLE_TORCH, DIAL_PHONE, WHATSAPP, CUSTOM_AI
    val actionPayload: String, // Target URL, app id, phone, or custom prompt
    val speakResponse: String, // Speech output by ARIC
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_history")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sender: String, // "user" or "aric"
    val message: String,
    val actionTaken: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
