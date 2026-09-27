package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AricDao {

    // === MEMORIES ===
    @Query("SELECT * FROM memories ORDER BY timestamp DESC")
    fun getAllMemories(): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memories WHERE text LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchMemories(query: String): Flow<List<MemoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryEntity): Long

    @Delete
    suspend fun deleteMemory(memory: MemoryEntity)

    @Query("DELETE FROM memories WHERE id = :id")
    suspend fun deleteMemoryById(id: Long)

    @Query("SELECT * FROM memories ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastMemory(): MemoryEntity?

    @Query("DELETE FROM memories")
    suspend fun clearAllMemories()

    // === ROUTINES / PROGRAMMABLE COMMANDS ===
    @Query("SELECT * FROM routines ORDER BY createdAt DESC")
    fun getAllRoutines(): Flow<List<RoutineEntity>>

    @Query("SELECT * FROM routines WHERE isEnabled = 1")
    suspend fun getEnabledRoutinesList(): List<RoutineEntity>

    @Query("SELECT * FROM routines WHERE isEnabled = 1")
    fun getEnabledRoutines(): Flow<List<RoutineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: RoutineEntity): Long

    @Update
    suspend fun updateRoutine(routine: RoutineEntity)

    @Delete
    suspend fun deleteRoutine(routine: RoutineEntity)

    @Query("DELETE FROM routines WHERE id = :id")
    suspend fun deleteRoutineById(id: Long)

    // === CHAT / CONVERSATION HISTORY ===
    @Query("SELECT * FROM chat_history ORDER BY timestamp ASC LIMIT 100")
    fun getRecentChat(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatMessageEntity): Long

    @Query("DELETE FROM chat_history")
    suspend fun clearChat()
}
