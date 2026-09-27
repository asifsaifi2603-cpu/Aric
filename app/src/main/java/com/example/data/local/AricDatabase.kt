package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [MemoryEntity::class, RoutineEntity::class, ChatMessageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AricDatabase : RoomDatabase() {

    abstract fun aricDao(): AricDao

    companion object {
        @Volatile
        private var INSTANCE: AricDatabase? = null

        fun getDatabase(context: Context): AricDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AricDatabase::class.java,
                    "aric_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                val dao = getDatabase(context).aricDao()
                                prePopulateDefaults(dao)
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun prePopulateDefaults(dao: AricDao) {
            // ARIC Initial Greeting
            dao.insertChat(
                ChatMessageEntity(
                    sender = "aric",
                    message = "Greetings Boss! ARIC online. Main aapke phone ke hardware, automation aur AI protocols ka full control lene ke liye ready hoon. Hukam kijiye Boss!",
                    actionTaken = "ARIC Core Online"
                )
            )

            // Jarvis Starter Protocols / Programmable Routines
            dao.insertRoutine(
                RoutineEntity(
                    title = "Protocol Alpha: Morning Briefing",
                    triggerPhrase = "morning briefing",
                    actionType = "SPEAK",
                    actionPayload = "Good morning Boss! All phone systems are online. Battery, network, and memory are optimal. Ready for your directives today.",
                    speakResponse = "Good morning Boss! All phone systems are online. Battery, network, and memory are optimal. Ready for your directives today."
                )
            )

            dao.insertRoutine(
                RoutineEntity(
                    title = "Protocol Sentinel: Tactical Torch",
                    triggerPhrase = "torch jalao",
                    actionType = "TOGGLE_TORCH",
                    actionPayload = "toggle",
                    speakResponse = "Tactical flashlight activated, Boss."
                )
            )

            dao.insertRoutine(
                RoutineEntity(
                    title = "Protocol Overdrive: Focus Mode",
                    triggerPhrase = "focus mode",
                    actionType = "SPEAK",
                    actionPayload = "Focus mode engaged, Boss. Silence and peak productivity active.",
                    speakResponse = "Focus mode engaged, Boss. Silence and peak productivity active."
                )
            )

            dao.insertRoutine(
                RoutineEntity(
                    title = "Protocol Media: YouTube Station",
                    triggerPhrase = "music chalao",
                    actionType = "OPEN_URL",
                    actionPayload = "https://www.youtube.com/results?search_query=best+workout+music",
                    speakResponse = "Right away Boss, launching music on YouTube."
                )
            )

            // Initial Boss Identity Memory
            dao.insertMemory(
                MemoryEntity(
                    text = "Owner & Master: Boss",
                    category = "Authorization"
                )
            )
            dao.insertMemory(
                MemoryEntity(
                    text = "Role: ARIC Elite Mobile AI Assistant",
                    category = "Core Protocol"
                )
            )
        }
    }
}
