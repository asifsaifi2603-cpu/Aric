package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiService
import com.example.data.local.AricDatabase
import com.example.data.local.ChatMessageEntity
import com.example.data.local.MemoryEntity
import com.example.data.local.RoutineEntity
import com.example.data.repository.AricRepository
import com.example.data.repository.AricSettings
import com.example.service.DeviceActionHandler
import com.example.service.DeviceTelemetry
import com.example.service.VoiceAssistantManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AricViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AricDatabase.getDatabase(application)
    val repository = AricRepository(db.aricDao(), application)
    val deviceActionHandler = DeviceActionHandler(application)
    private val geminiService = GeminiService()

    private val _userSpeechInput = MutableStateFlow("")
    val userSpeechInput = _userSpeechInput.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing = _isProcessing.asStateFlow()

    private val _statusMessage = MutableStateFlow("ARIC Online. Standing by for Boss.")
    val statusMessage = _statusMessage.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _telemetry = MutableStateFlow(deviceActionHandler.getDeviceTelemetry())
    val telemetry: StateFlow<DeviceTelemetry> = _telemetry.asStateFlow()

    // Settings
    val settings: StateFlow<AricSettings> = repository.settings

    // Voice assistant manager
    val voiceManager: VoiceAssistantManager = VoiceAssistantManager(
        context = application,
        onSpeechResult = { text ->
            processUserCommand(text)
        },
        onError = { err ->
            _errorMessage.value = err
        }
    )

    // Data streams from Room
    val chatHistory: StateFlow<List<ChatMessageEntity>> = repository.chatHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val memories: StateFlow<List<MemoryEntity>> = repository.allMemories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val routines: StateFlow<List<RoutineEntity>> = repository.allRoutines
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun refreshTelemetry() {
        _telemetry.value = deviceActionHandler.getDeviceTelemetry()
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun startListening() {
        _errorMessage.value = null
        voiceManager.startListening()
    }

    fun stopListening() {
        voiceManager.stopListening()
    }

    fun toggleListening() {
        if (voiceManager.isListening.value) {
            voiceManager.stopListening()
        } else {
            startListening()
        }
    }

    fun stopSpeaking() {
        voiceManager.stopSpeaking()
    }

    fun submitTextCommand(command: String) {
        if (command.isNotBlank()) {
            processUserCommand(command.trim())
        }
    }

    fun runJarvisSystemScan() {
        viewModelScope.launch {
            refreshTelemetry()
            val t = _telemetry.value
            val netStatus = if (t.isWifiConnected) "Wi-Fi se connected" else if (t.isCellularConnected) "Mobile Data active" else "Offline"
            val chargeStatus = if (t.isCharging) "charging par hai" else "battery backup normal hai"
            val speech = "Boss, system diagnostic scan complete. Battery ${t.batteryPercent}% hai aur $chargeStatus. Network $netStatus hai. ${t.availableRamMb} MB RAM available hai. All ARIC protocols 100% operational hain!"
            speakAndLog(speech, "System Diagnostic Scan")
        }
    }

    fun runMorningBriefing() {
        viewModelScope.launch {
            refreshTelemetry()
            val t = _telemetry.value
            val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
            val date = SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(Date())
            val boss = settings.value.ownerName
            val speech = "Shubh prabhat $boss! Abhi samay $time hai, aur aaj $date hai. Battery ${t.batteryPercent}% hai. Aapka phone fully ready hai. Bataiye aaj kya directives hain?"
            speakAndLog(speech, "Morning Briefing")
        }
    }

    private fun processUserCommand(rawCommand: String) {
        viewModelScope.launch {
            _isProcessing.value = true
            _statusMessage.value = "Processing command for Boss..."

            val cleaned = cleanWakeWords(rawCommand)
            val lower = cleaned.lowercase().trim()

            // 1. Add user message to chat history
            repository.addChatMessage(sender = "user", message = rawCommand)

            // 2. Check user's custom PROGRAMMABLE ROUTINES first ("jo me programm kar saku")
            val enabledRoutines = repository.getEnabledRoutinesList()
            val matchedRoutine = enabledRoutines.firstOrNull { routine ->
                val trig = routine.triggerPhrase.lowercase().trim()
                lower == trig || lower.contains(trig) || trig.contains(lower)
            }

            if (matchedRoutine != null) {
                executeRoutine(matchedRoutine)
                _isProcessing.value = false
                return@launch
            }

            // 3. Check Jarvis Protocols & Local Device Actions
            val handledAction = handleDeviceCommand(lower, cleaned)
            if (handledAction != null) {
                val (speechResponse, actionBadge) = handledAction
                speakAndLog(speechResponse, actionBadge)
                _isProcessing.value = false
                return@launch
            }

            // 4. Check Memory Commands ("yaad rakho", "remember", etc.)
            val handledMemory = handleMemoryCommand(lower, cleaned)
            if (handledMemory != null) {
                val (speechResponse, actionBadge) = handledMemory
                speakAndLog(speechResponse, actionBadge)
                _isProcessing.value = false
                return@launch
            }

            // 5. Query AI Brain (Gemini) with ARIC Persona & Boss injected context
            _statusMessage.value = "Consulting ARIC Neural Core..."
            val memoryList = memories.value.map { it.text }
            val systemPrompt = settings.value.customSystemPrompt + " Always address the user loyally and respectfully as 'Boss' or '${settings.value.ownerName}'. Give sharp, intelligent, concise answers."
            val aiResponse = geminiService.askGemini(
                userPrompt = rawCommand,
                systemInstruction = systemPrompt,
                userMemories = memoryList,
                customKey = settings.value.apiKey
            )

            speakAndLog(aiResponse, "ARIC Intelligence")
            _isProcessing.value = false
            _statusMessage.value = "ARIC ready for Boss."
        }
    }

    private suspend fun executeRoutine(routine: RoutineEntity) {
        var actionLabel = "Protocol: ${routine.title}"
        var speech = routine.speakResponse

        when (routine.actionType) {
            "SPEAK" -> {
                // Just speak
            }
            "OPEN_APP" -> {
                when (routine.actionPayload.lowercase().trim()) {
                    "camera" -> deviceActionHandler.openCamera()
                    "calculator" -> deviceActionHandler.openCalculator()
                    "youtube" -> deviceActionHandler.openYouTube()
                    "settings" -> deviceActionHandler.openSystemSettings()
                    "maps" -> deviceActionHandler.openMaps()
                    "alarms", "alarm" -> deviceActionHandler.openAlarms()
                    else -> deviceActionHandler.openUrl(routine.actionPayload)
                }
                actionLabel = "App Launched"
            }
            "OPEN_URL" -> {
                deviceActionHandler.openUrl(routine.actionPayload)
                actionLabel = "Web Opened"
            }
            "TOGGLE_TORCH" -> {
                deviceActionHandler.toggleTorch()
                actionLabel = "Torch Toggled"
            }
            "DIAL_PHONE" -> {
                deviceActionHandler.dialPhoneNumber(routine.actionPayload)
                actionLabel = "Dialer Opened"
            }
            "WHATSAPP" -> {
                deviceActionHandler.openWhatsApp(routine.actionPayload)
                actionLabel = "WhatsApp Opened"
            }
            "CUSTOM_AI" -> {
                val aiAnswer = geminiService.askGemini(
                    userPrompt = routine.actionPayload,
                    systemInstruction = settings.value.customSystemPrompt,
                    userMemories = memories.value.map { it.text },
                    customKey = settings.value.apiKey
                )
                speech = aiAnswer
                actionLabel = "Custom AI"
            }
        }

        speakAndLog(speech, actionLabel)
    }

    private suspend fun handleDeviceCommand(lower: String, original: String): Pair<String, String>? {
        val boss = settings.value.ownerName

        // Jarvis Diagnostic Scan
        if (lower.contains("system scan") || lower.contains("diagnostic scan") || lower.contains("scan karo") ||
            lower.contains("diagnostics") || lower.contains("phone check") || lower.contains("system status")
        ) {
            refreshTelemetry()
            val t = _telemetry.value
            val net = if (t.isWifiConnected) "Wi-Fi active" else if (t.isCellularConnected) "Mobile Data active" else "Offline"
            val chg = if (t.isCharging) "charging state me" else "battery mode me"
            return Pair(
                "Yes $boss! Diagnostic complete. Battery ${t.batteryPercent}% $chg, network $net, ${t.availableRamMb} MB memory available. All phone systems are green!",
                "Jarvis Diagnostic"
            )
        }

        // Morning Briefing
        if (lower.contains("morning briefing") || lower.contains("subah ka update") || lower.contains("briefing")) {
            refreshTelemetry()
            val t = _telemetry.value
            val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
            val date = SimpleDateFormat("d MMMM", Locale.getDefault()).format(Date())
            return Pair(
                "Good morning $boss! Samay hai $time, $date. Battery ${t.batteryPercent}% par hai. Main aapke orders ke liye taiyar hoon.",
                "Morning Briefing"
            )
        }

        // Volume controls
        if (lower.contains("volume up") || lower.contains("volume badhao") || lower.contains("awaz badhao")) {
            deviceActionHandler.volumeUp()
            return Pair("Volume badha diya hai, $boss.", "Volume Raised")
        }

        if (lower.contains("volume down") || lower.contains("volume kam karo") || lower.contains("awaz kam karo")) {
            deviceActionHandler.volumeDown()
            return Pair("Volume kam kar diya hai, $boss.", "Volume Lowered")
        }

        if (lower.contains("mute") || lower.contains("silent karo") || lower.contains("awaz band karo")) {
            deviceActionHandler.muteVolume()
            return Pair("Audio mute kar diya hai, $boss.", "Audio Muted")
        }

        // Battery check
        if (lower.contains("battery") || lower.contains("charge kitna") || lower.contains("battery percentage")) {
            refreshTelemetry()
            val t = _telemetry.value
            val chg = if (t.isCharging) "aur phone charge ho raha hai." else "hai."
            return Pair("$boss, battery abhi ${t.batteryPercent}% $chg", "Battery Check")
        }

        // Alarms & Timers
        if (lower.contains("alarm") || lower.contains("alarm kholo") || lower.contains("alarm lagao")) {
            deviceActionHandler.openAlarms()
            return Pair("Alarms open kar diye hain, $boss.", "Alarms Opened")
        }

        if (lower.contains("timer") || lower.contains("timer lagao")) {
            deviceActionHandler.setQuickTimer(300, "ARIC Boss Timer")
            return Pair("5 minute ka quick timer set kar diya hai, $boss.", "Timer Set")
        }

        // Maps & Location
        if (lower.contains("maps kholo") || lower.contains("open maps") || lower.contains("navigation") || lower.contains("kahan hoon")) {
            deviceActionHandler.openMaps()
            return Pair("Navigation open kar diya hai, $boss.", "Maps Launched")
        }

        // Torch / Flashlight
        if (lower.contains("torch on") || lower.contains("torch jalao") || lower.contains("flashlight on") ||
            lower.contains("roshni karo") || lower.contains("batti jalao") || lower.contains("light on")
        ) {
            deviceActionHandler.setTorch(true)
            return Pair("Flashlight on kar di hai, $boss.", "Torch Activated")
        }

        if (lower.contains("torch off") || lower.contains("torch band") || lower.contains("flashlight off") ||
            lower.contains("batti bujhao") || lower.contains("light off")
        ) {
            deviceActionHandler.setTorch(false)
            return Pair("Flashlight band kar di hai, $boss.", "Torch Deactivated")
        }

        if (lower == "torch" || lower == "flashlight") {
            val result = deviceActionHandler.toggleTorch()
            val state = if (result.getOrDefault(false)) "on" else "off"
            return Pair("Flashlight $state kar di hai, $boss.", "Torch Toggled")
        }

        // Camera
        if (lower.contains("camera kholo") || lower.contains("open camera") || lower.contains("photo kheecho") || lower.contains("take photo")) {
            deviceActionHandler.openCamera()
            return Pair("Right away $boss! Camera launch kar diya hai.", "Camera Launched")
        }

        // Calculator
        if (lower.contains("calculator kholo") || lower.contains("open calculator") || lower.contains("hisab karo")) {
            deviceActionHandler.openCalculator()
            return Pair("Calculator open kar diya hai, $boss.", "Calculator Opened")
        }

        // YouTube
        if (lower.startsWith("youtube par ") || lower.startsWith("youtube pe ")) {
            val query = original.substring(original.indexOf(" ", 7) + 1).trim()
            deviceActionHandler.openYouTube(query)
            return Pair("YouTube par '$query' search kar diya hai, $boss.", "YouTube Search")
        }

        if (lower.contains("youtube kholo") || lower.contains("open youtube") || lower == "youtube") {
            deviceActionHandler.openYouTube()
            return Pair("YouTube open kar diya hai, $boss.", "YouTube Opened")
        }

        // Google search
        val searchPrefixes = listOf("google par ", "google pe ", "search karo ", "search for ", "search ")
        for (prefix in searchPrefixes) {
            if (lower.startsWith(prefix)) {
                val query = original.substring(prefix.length).trim()
                if (query.isNotBlank()) {
                    deviceActionHandler.openGoogleSearch(query)
                    return Pair("Google par '$query' search kar diya, $boss.", "Google Search")
                }
            }
        }

        if (lower.contains("google kholo") || lower.contains("open google") || lower == "google") {
            deviceActionHandler.openUrl("https://www.google.com")
            return Pair("Google open kar diya hai, $boss.", "Google Opened")
        }

        // Time
        if (lower.contains("time") || lower.contains("kitne baje") || lower.contains("samay kya")) {
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val time = timeFormat.format(Date())
            return Pair("$boss, abhi $time ho rahe hain.", "Time Check")
        }

        // Date
        if (lower.contains("aaj ki date") || lower.contains("today date") || lower.contains("tarikh kya hai") || lower.contains("what is the date")) {
            val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
            val date = dateFormat.format(Date())
            return Pair("$boss, aaj ki date $date hai.", "Date Check")
        }

        // Settings
        if (lower.contains("wifi kholo") || lower.contains("wifi settings") || lower.contains("open wifi")) {
            deviceActionHandler.openWifiSettings()
            return Pair("Wi-Fi control panel open kar diya hai, $boss.", "Wi-Fi Settings")
        }

        if (lower.contains("bluetooth kholo") || lower.contains("bluetooth settings") || lower.contains("open bluetooth")) {
            deviceActionHandler.openBluetoothSettings()
            return Pair("Bluetooth settings open kar di hain, $boss.", "Bluetooth Settings")
        }

        if (lower.contains("battery settings") || lower.contains("battery saver")) {
            deviceActionHandler.openBatterySettings()
            return Pair("Power management open kar diya hai, $boss.", "Battery Settings")
        }

        if (lower.contains("settings kholo") || lower.contains("open settings") || lower.contains("phone settings")) {
            deviceActionHandler.openSystemSettings()
            return Pair("System settings open kar di hain, $boss.", "System Settings")
        }

        // Call / Phone
        if (lower.startsWith("call ") || lower.contains("ko call lagao") || lower.contains("phone lagao")) {
            val digitsOnly = original.filter { it.isDigit() }
            if (digitsOnly.length >= 3) {
                deviceActionHandler.dialPhoneNumber(digitsOnly)
                return Pair("Number $digitsOnly dialer me load kar diya hai, $boss.", "Phone Dialer")
            } else {
                deviceActionHandler.dialPhoneNumber("")
                return Pair("Phone dialer open kar diya hai, $boss.", "Phone Dialer")
            }
        }

        // WhatsApp
        if (lower.contains("whatsapp kholo") || lower.contains("open whatsapp") || lower.contains("whatsapp message")) {
            deviceActionHandler.openWhatsApp()
            return Pair("WhatsApp open kar diya hai, $boss.", "WhatsApp Opened")
        }

        // Standby
        if (lower in listOf("stop", "exit", "chup ho jao", "band ho jao", "bye", "goodbye", "sleep", "so jao")) {
            voiceManager.stopSpeaking()
            voiceManager.stopListening()
            return Pair("As you wish, $boss. ARIC going to standby mode.", "Standby")
        }

        return null
    }

    private suspend fun handleMemoryCommand(lower: String, original: String): Pair<String, String>? {
        val boss = settings.value.ownerName

        // Yaad rakho / Remember
        if (lower.startsWith("yaad rakho ") || lower.startsWith("remember ")) {
            val prefix = if (lower.startsWith("yaad rakho ")) "yaad rakho " else "remember "
            val note = original.substring(prefix.length).trim()
            if (note.isNotBlank()) {
                repository.addMemory(note)
                return Pair("ARIC memory vault me secure kar liya gaya hai, $boss: $note", "Memory Secured")
            }
        }

        // Show memory
        if (lower.contains("meri memory dikhao") || lower.contains("what do you remember") || lower.contains("memory dikhao")) {
            val all = memories.value
            return if (all.isEmpty()) {
                Pair("$boss, meri memory me abhi koi data saved nahi hai.", "Memory Checked")
            } else {
                val items = all.take(5).joinToString(" | ") { it.text }
                Pair("$boss, memory vault me ye saved hai: $items", "Memory List")
            }
        }

        // Forget last memory
        if (lower.contains("last memory hatao") || lower.contains("forget last memory") || lower.contains("aakhri memory delete")) {
            val removed = repository.deleteLastMemory()
            return if (removed != null) {
                Pair("Last memory database se erase kar di gayi hai, $boss: ${removed.text}", "Memory Erased")
            } else {
                Pair("Memory me kuch delete karne ke liye nahi hai, $boss.", "Memory Empty")
            }
        }

        return null
    }

    private fun cleanWakeWords(input: String): String {
        var t = input.trim()
        val wakeWords = listOf("hey aric ", "aric ", "jarvis ", "हे एरिक ", "एरिक ")
        for (w in wakeWords) {
            if (t.startsWith(w, ignoreCase = true)) {
                t = t.substring(w.length).trim()
                break
            }
        }
        return t
    }

    private suspend fun speakAndLog(text: String, actionBadge: String) {
        _statusMessage.value = "ARIC: $text"
        repository.addChatMessage(sender = "aric", message = text, actionTaken = actionBadge)
        voiceManager.speak(
            text = text,
            rate = settings.value.speechRate,
            pitch = settings.value.speechPitch
        )
    }

    // CRUD for Routines (Programmer Studio)
    fun createOrUpdateRoutine(
        id: Long = 0,
        title: String,
        triggerPhrase: String,
        actionType: String,
        actionPayload: String,
        speakResponse: String,
        isEnabled: Boolean = true
    ) {
        viewModelScope.launch {
            repository.saveRoutine(
                RoutineEntity(
                    id = id,
                    title = title.trim(),
                    triggerPhrase = triggerPhrase.trim().lowercase(),
                    actionType = actionType,
                    actionPayload = actionPayload.trim(),
                    speakResponse = speakResponse.trim(),
                    isEnabled = isEnabled
                )
            )
        }
    }

    fun toggleRoutineState(routine: RoutineEntity) {
        viewModelScope.launch {
            repository.toggleRoutine(routine, !routine.isEnabled)
        }
    }

    fun deleteRoutine(routine: RoutineEntity) {
        viewModelScope.launch {
            repository.deleteRoutine(routine)
        }
    }

    fun testRoutine(routine: RoutineEntity) {
        viewModelScope.launch {
            executeRoutine(routine)
        }
    }

    // Bulk JSON Import & Export for Python aric_memory.json or Custom User Backups
    fun importMemoriesFromJson(jsonString: String): Int {
        var count = 0
        try {
            val trimmed = jsonString.trim()
            if (trimmed.startsWith("{")) {
                val obj = org.json.JSONObject(trimmed)
                val notesArray = obj.optJSONArray("notes")
                if (notesArray != null) {
                    for (i in 0 until notesArray.length()) {
                        val item = notesArray.opt(i)
                        val text = when (item) {
                            is org.json.JSONObject -> item.optString("text")
                            is String -> item
                            else -> item.toString()
                        }
                        if (text.isNotBlank()) {
                            addManualMemory(text, "Imported")
                            count++
                        }
                    }
                }
            } else if (trimmed.startsWith("[")) {
                val array = org.json.JSONArray(trimmed)
                for (i in 0 until array.length()) {
                    val item = array.opt(i)
                    val text = when (item) {
                        is org.json.JSONObject -> item.optString("text")
                        is String -> item
                        else -> item.toString()
                    }
                    if (text.isNotBlank()) {
                        addManualMemory(text, "Imported")
                        count++
                    }
                }
            } else {
                // Line separated notes
                val lines = trimmed.lines()
                for (line in lines) {
                    val clean = line.removePrefix("-").removePrefix("•").trim()
                    if (clean.isNotBlank()) {
                        addManualMemory(clean, "Imported")
                        count++
                    }
                }
            }
        } catch (_: Exception) {}
        return count
    }

    fun exportMemoriesJson(): String {
        val list = memories.value
        val obj = org.json.JSONObject()
        val arr = org.json.JSONArray()
        list.forEach { mem ->
            val m = org.json.JSONObject()
            m.put("text", mem.text)
            m.put("category", mem.category)
            m.put("timestamp", mem.timestamp)
            arr.put(m)
        }
        obj.put("notes", arr)
        obj.put("exported_by", "ARIC Mobile Jarvis")
        return obj.toString(2)
    }

    // CRUD for Memories
    fun addManualMemory(text: String, category: String = "Personal") {
        viewModelScope.launch {
            if (text.isNotBlank()) {
                repository.addMemory(text, category)
            }
        }
    }

    fun deleteMemory(memory: MemoryEntity) {
        viewModelScope.launch {
            repository.deleteMemory(memory)
        }
    }

    fun clearAllMemories() {
        viewModelScope.launch {
            repository.clearAllMemories()
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            repository.clearChat()
        }
    }

    fun saveSettings(newSettings: AricSettings) {
        repository.updateSettings(newSettings)
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.destroy()
    }
}
