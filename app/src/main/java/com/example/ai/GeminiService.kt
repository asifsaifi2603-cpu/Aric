package com.example.ai

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun askGemini(
        userPrompt: String,
        systemInstruction: String,
        userMemories: List<String> = emptyList(),
        customKey: String? = null
    ): String = withContext(Dispatchers.IO) {
        val apiKey = when {
            !customKey.isNullOrBlank() -> customKey.trim()
            tryGetBuildConfigKey().isNotBlank() -> tryGetBuildConfigKey()
            else -> ""
        }

        // If no valid key is configured, provide our rich local ARIC assistant intelligence
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext generateLocalAricResponse(userPrompt, userMemories)
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val memoryContext = if (userMemories.isNotEmpty()) {
                "\nUser Personal Memories & Facts:\n" + userMemories.joinToString("\n") { "- $it" }
            } else ""

            val fullSystemPrompt = "$systemInstruction$memoryContext\nKeep responses crisp, conversational, and direct for mobile speech output. Support Hindi, Hinglish, and English naturally."

            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", userPrompt))
                        })
                    })
                }
                put("contents", contentsArray)

                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", fullSystemPrompt))
                    })
                })
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestJson.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                // If API fails (e.g. quota, bad key), gracefully fallback to local intelligent response
                return@withContext generateLocalAricResponse(userPrompt, userMemories)
            }

            val json = JSONObject(responseBody)
            val candidates = json.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text")

            if (!text.isNullOrBlank()) {
                text.trim()
            } else {
                generateLocalAricResponse(userPrompt, userMemories)
            }
        } catch (e: Exception) {
            generateLocalAricResponse(userPrompt, userMemories)
        }
    }

    private fun tryGetBuildConfigKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Throwable) {
            ""
        }
    }

    /**
     * Local intelligence fallback that handles common questions, identity,
     * jokes, memory lookups, and friendly conversational responses in Hindi/Hinglish.
     */
    private fun generateLocalAricResponse(prompt: String, memories: List<String>): String {
        val lower = prompt.lowercase().trim()

        // Identity
        if (lower.contains("who are you") || lower.contains("kaun ho") || lower.contains("kya ho")) {
            return "Main ARIC hoon — aapka personal AI assistant! Main aapke phone ke tools control kar sakta hoon, baatein yaad rakh sakta hoon, aur aapke custom programmed commands chala sakta hoon."
        }

        // Creator
        if (lower.contains("who made you") || lower.contains("kisne banaya")) {
            return "Mujhe Asif Saifi ne design aur develop kiya hai ek modern personal AI assistant ke roop me!"
        }

        // Capabilities
        if (lower.contains("kya kar sakte ho") || lower.contains("what can you do") || lower.contains("help") || lower.contains("madad")) {
            return "Main YouTube/Google khol sakta hoon, camera launch kar sakta hoon, torch on/off kar sakta hoon, time aur date bata sakta hoon, aapki baatein yaad rakh sakta hoon, aur 'Programmer' tab me aap mujhme naye commands program kar sakte hain!"
        }

        // Memory check
        if (lower.contains("memory") || lower.contains("yaad")) {
            return if (memories.isNotEmpty()) {
                "Meri memory me ye saved hai:\n" + memories.take(5).joinToString("\n") { "• $it" }
            } else {
                "Meri memory me abhi kuch saved nahi hai. Aap 'yaad rakho [baat]' bol kar kuch bhi save kar sakte hain."
            }
        }

        // Greetings
        if (lower.contains("namaste") || lower.contains("hello") || lower.contains("hi aric") || lower.contains("hey")) {
            return "Namaste! Ji boliye, main aapke fone me kya help kar sakta hoon?"
        }

        // How are you
        if (lower.contains("kaise ho") || lower.contains("how are you")) {
            return "Main bilkul badhiya hoon aur aapke commands ke liye fully ready hoon! Aap bataiye, aaj kya plan hai?"
        }

        // Jokes
        if (lower.contains("joke") || lower.contains("chutkula") || lower.contains("hansaao")) {
            val jokes = listOf(
                "Ek programmer doctor ke paas gaya. Doctor ne pucha: 'Neend kitne ghante aati hai?' Programmer bola: 'Neend ka toh pata nahi, par error solving 100% hai!'",
                "Teacher: Beta homework kyu nahi kiya? Student: Sir, electricity chali gayi thi. Teacher: Toh candle jala lete! Student: Matchbox fridge me tha aur fridge me andhera tha!",
                "Debug karte waqt ek developer ko laga ki sab theek ho gaya... phir usne Run dabaya!"
            )
            return jokes.random()
        }

        // Fallback natural answer
        return "Maine aapki baat samajh li: '$prompt'. Main aapka request process kar raha hoon. Aap chaho toh iske liye 'Programmer' me custom voice trigger bhi bana sakte ho!"
    }
}
