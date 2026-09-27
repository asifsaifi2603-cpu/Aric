package com.example.service

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import java.net.URLEncoder

data class DeviceTelemetry(
    val batteryPercent: Int,
    val isCharging: Boolean,
    val isWifiConnected: Boolean,
    val isCellularConnected: Boolean,
    val availableRamMb: Long,
    val totalRamMb: Long,
    val deviceModel: String,
    val osVersion: String
)

class DeviceActionHandler(private val context: Context) {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private var isTorchOn: Boolean = false

    fun triggerHapticFeedback() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(50)
            }
        } catch (_: Exception) {}
    }

    // === SYSTEM TELEMETRY (JARVIS SCANNER) ===
    fun getDeviceTelemetry(): DeviceTelemetry {
        // Battery
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 50
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
        val batteryPercent = if (scale > 0) (level * 100 / scale) else 50
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        // Network
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val activeNetwork = cm?.activeNetwork
        val caps = cm?.getNetworkCapabilities(activeNetwork)
        val isWifi = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        val isCellular = caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true

        // RAM Memory
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        am?.getMemoryInfo(memoryInfo)
        val availMb = memoryInfo.availMem / (1024 * 1024)
        val totalMb = memoryInfo.totalMem / (1024 * 1024)

        return DeviceTelemetry(
            batteryPercent = batteryPercent,
            isCharging = isCharging,
            isWifiConnected = isWifi,
            isCellularConnected = isCellular,
            availableRamMb = availMb,
            totalRamMb = totalMb,
            deviceModel = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}",
            osVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
        )
    }

    // === VOLUME CONTROL ===
    fun volumeUp(): Boolean {
        return try {
            audioManager?.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_RAISE,
                AudioManager.FLAG_SHOW_UI
            )
            triggerHapticFeedback()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun volumeDown(): Boolean {
        return try {
            audioManager?.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_LOWER,
                AudioManager.FLAG_SHOW_UI
            )
            triggerHapticFeedback()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun muteVolume(): Boolean {
        return try {
            audioManager?.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                0,
                AudioManager.FLAG_SHOW_UI
            )
            triggerHapticFeedback()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun setVolume(percentage: Int): Boolean {
        return try {
            val max = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15
            val target = (max * percentage.coerceIn(0, 100)) / 100
            audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, target, AudioManager.FLAG_SHOW_UI)
            triggerHapticFeedback()
            true
        } catch (e: Exception) {
            false
        }
    }

    // === TORCH / FLASHLIGHT ===
    fun toggleTorch(): Result<Boolean> {
        if (cameraManager == null) return Result.failure(Exception("Camera hardware not available"))
        try {
            val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                val chars = cameraManager.getCameraCharacteristics(id)
                val hasFlash = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                val facing = chars.get(CameraCharacteristics.LENS_FACING)
                hasFlash && facing == CameraCharacteristics.LENS_FACING_BACK
            } ?: cameraManager.cameraIdList.firstOrNull()

            if (cameraId == null) return Result.failure(Exception("No flashlight camera found"))

            isTorchOn = !isTorchOn
            cameraManager.setTorchMode(cameraId, isTorchOn)
            triggerHapticFeedback()
            return Result.success(isTorchOn)
        } catch (e: CameraAccessException) {
            return Result.failure(e)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    fun setTorch(enable: Boolean): Result<Boolean> {
        if (cameraManager == null) return Result.failure(Exception("No camera manager"))
        try {
            val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                val chars = cameraManager.getCameraCharacteristics(id)
                chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            } ?: cameraManager.cameraIdList.firstOrNull()

            if (cameraId != null) {
                cameraManager.setTorchMode(cameraId, enable)
                isTorchOn = enable
                triggerHapticFeedback()
                return Result.success(enable)
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return Result.failure(Exception("Unable to change torch state"))
    }

    // === ALARMS & TIMER ===
    fun openAlarms(): Boolean {
        return try {
            val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun setQuickTimer(seconds: Int, message: String = "ARIC Timer"): Boolean {
        return try {
            val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                putExtra(AlarmClock.EXTRA_LENGTH, seconds)
                putExtra(AlarmClock.EXTRA_MESSAGE, message)
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            openAlarms()
        }
    }

    // === MAPS & NAVIGATION ===
    fun openMaps(query: String? = null): Boolean {
        return try {
            val uri = if (!query.isNullOrBlank()) {
                Uri.parse("geo:0,0?q=" + URLEncoder.encode(query, "UTF-8"))
            } else {
                Uri.parse("geo:0,0?q=")
            }
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            openGoogleSearch("maps $query")
        }
    }

    // === CAMERA & CALCULATOR ===
    fun openCamera(): Boolean {
        return try {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            triggerHapticFeedback()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun openCalculator(): Boolean {
        val intents = listOf(
            Intent().apply {
                setAction(Intent.ACTION_MAIN)
                addCategory(Intent.CATEGORY_APP_CALCULATOR)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            },
            Intent(Intent.ACTION_MAIN).apply {
                setClassName("com.google.android.calculator", "com.android.calculator2.Calculator")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            },
            Intent(Intent.ACTION_MAIN).apply {
                setClassName("com.android.calculator2", "com.android.calculator2.Calculator")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        )

        for (intent in intents) {
            try {
                context.startActivity(intent)
                triggerHapticFeedback()
                return true
            } catch (_: Exception) {}
        }
        return false
    }

    // === WEB & APPS ===
    fun openYouTube(query: String? = null): Boolean {
        return try {
            val url = if (!query.isNullOrBlank()) {
                "https://www.youtube.com/results?search_query=" + URLEncoder.encode(query, "UTF-8")
            } else {
                "https://www.youtube.com"
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            triggerHapticFeedback()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun openGoogleSearch(query: String): Boolean {
        return try {
            val url = "https://www.google.com/search?q=" + URLEncoder.encode(query, "UTF-8")
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            triggerHapticFeedback()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun openUrl(url: String): Boolean {
        return try {
            val formatted = if (!url.startsWith("http://") && !url.startsWith("https://")) {
                "https://$url"
            } else url
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(formatted)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            triggerHapticFeedback()
            true
        } catch (e: Exception) {
            false
        }
    }

    // === SYSTEM SETTINGS ===
    fun openWifiSettings(): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            triggerHapticFeedback()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun openBluetoothSettings(): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            triggerHapticFeedback()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun openSystemSettings(): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            triggerHapticFeedback()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun openBatterySettings(): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            triggerHapticFeedback()
            true
        } catch (e: Exception) {
            try {
                val fallback = Intent(Intent.ACTION_POWER_USAGE_SUMMARY).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallback)
                true
            } catch (ex: Exception) {
                false
            }
        }
    }

    fun dialPhoneNumber(phoneNumber: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            triggerHapticFeedback()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun openWhatsApp(message: String = ""): Boolean {
        return try {
            val uri = if (message.isNotBlank()) {
                Uri.parse("https://api.whatsapp.com/send?text=" + URLEncoder.encode(message, "UTF-8"))
            } else {
                Uri.parse("https://api.whatsapp.com")
            }
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            triggerHapticFeedback()
            true
        } catch (e: Exception) {
            false
        }
    }
}
