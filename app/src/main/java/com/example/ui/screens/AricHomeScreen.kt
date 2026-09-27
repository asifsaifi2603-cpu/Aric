package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.ChatMessageEntity
import com.example.ui.AricViewModel
import com.example.ui.components.AricOrbVisualizer
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.NeonPurple
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AricHomeScreen(
    viewModel: AricViewModel,
    onNavigateToProgrammer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isListening by viewModel.voiceManager.isListening.collectAsState()
    val isSpeaking by viewModel.voiceManager.isSpeaking.collectAsState()
    val rmsLevel by viewModel.voiceManager.rmsLevel.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val chatHistory by viewModel.chatHistory.collectAsState()
    val partialTranscript by viewModel.voiceManager.partialTranscript.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()
    val settings by viewModel.settings.collectAsState()

    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Scroll to latest chat message
    LaunchedEffect(chatHistory.size) {
        if (chatHistory.isNotEmpty()) {
            listState.animateScrollToItem(chatHistory.size - 1)
        }
    }

    val quickCommands = listOf(
        "⚡ System Scan",
        "🌅 Morning Briefing",
        "🔦 Torch on",
        "🔊 Volume Up",
        "📷 Camera kholo",
        "▶️ YouTube music",
        "🗺️ Maps kholo",
        "⏰ Alarms kholo",
        "🧠 Meri memory dikhao",
        "💡 Coding focus",
        "😂 Ek joke sunao"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // === JARVIS HUD Top Status Bar ===
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.25f))
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isListening || isSpeaking) NeonEmerald else CyanPrimary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ARIC AI CORE • BOSS ACTIVE",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.1.sp,
                                color = CyanPrimary
                            )
                        )
                    }

                    Surface(
                        color = if (isListening) CyanPrimary.copy(alpha = 0.2f) else DarkSurfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = when {
                                isListening -> "LISTENING"
                                isProcessing -> "ANALYZING"
                                isSpeaking -> "TRANSMITTING"
                                else -> "ARIC ONLINE"
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isListening) CyanPrimary else NeonEmerald
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Real-time Device Telemetry Strip (Battery, WiFi, RAM)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.BatteryChargingFull,
                            contentDescription = "Battery",
                            tint = if (telemetry.batteryPercent > 20) NeonEmerald else Color(0xFFEF4444),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${telemetry.batteryPercent}% ${if (telemetry.isCharging) "⚡" else ""}",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Wifi,
                            contentDescription = "Network",
                            tint = if (telemetry.isWifiConnected) CyanPrimary else Color(0xFF94A3B8),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (telemetry.isWifiConnected) "Wi-Fi" else if (telemetry.isCellularConnected) "4G/5G" else "Offline",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                        )
                    }

                    Text(
                        text = "RAM: ${telemetry.availableRamMb}MB Free",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                    )

                    Text(
                        text = telemetry.deviceModel.take(14),
                        style = MaterialTheme.typography.labelSmall.copy(color = CyanPrimary, fontSize = 10.sp)
                    )
                }
            }
        }

        // === Jarvis Holographic Arc-Reactor Orb ===
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            AricOrbVisualizer(
                isListening = isListening,
                isSpeaking = isSpeaking,
                isProcessing = isProcessing,
                rmsLevel = rmsLevel,
                size = 136.dp,
                onClick = { viewModel.toggleListening() }
            )
        }

        // Live Subtitle / Voice Status Banner
        AnimatedVisibility(
            visible = isListening || isSpeaking || isProcessing,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 2.dp),
                color = DarkSurfaceVariant.copy(alpha = 0.7f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = if (isListening) "🎙️ $partialTranscript" else "ARIC: $statusMessage",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = CyanPrimary,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    maxLines = 2
                )
            }
        }

        // === Boss Master Directive Action Bar ===
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                DirectiveButton(
                    label = "System Scan",
                    icon = Icons.Default.Radar,
                    color = CyanPrimary,
                    onClick = { viewModel.runJarvisSystemScan() }
                )
            }
            item {
                DirectiveButton(
                    label = "Morning Briefing",
                    icon = Icons.Default.WbSunny,
                    color = Color(0xFFF59E0B),
                    onClick = { viewModel.runMorningBriefing() }
                )
            }
            item {
                DirectiveButton(
                    label = "Torch",
                    icon = Icons.Default.FlashOn,
                    color = NeonEmerald,
                    onClick = { viewModel.deviceActionHandler.toggleTorch() }
                )
            }
            item {
                DirectiveButton(
                    label = "Vol +",
                    icon = Icons.Default.VolumeUp,
                    color = NeonIndigo,
                    onClick = { viewModel.deviceActionHandler.volumeUp() }
                )
            }
            item {
                DirectiveButton(
                    label = "Camera",
                    icon = Icons.Default.CameraAlt,
                    color = NeonPurple,
                    onClick = { viewModel.deviceActionHandler.openCamera() }
                )
            }
        }

        // === Quick Command Chips ===
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(quickCommands) { cmd ->
                AssistChip(
                    onClick = {
                        val cleanCmd = cmd.substringAfter(" ")
                        viewModel.submitTextCommand(cleanCmd)
                    },
                    label = { Text(cmd, fontSize = 11.sp) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = DarkSurfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = AssistChipDefaults.assistChipBorder(
                        borderColor = CyanPrimary.copy(alpha = 0.2f),
                        enabled = true
                    )
                )
            }
        }

        // === Real-Time Conversation Stream ===
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 6.dp)
        ) {
            items(chatHistory) { msg ->
                ChatBubble(message = msg, onSpeakAgain = {
                    if (msg.sender == "aric") {
                        viewModel.voiceManager.speak(msg.message)
                    }
                })
            }
        }

        // === Bottom Control & Input Bar ===
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = DarkSurface,
            tonalElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("command_input"),
                    placeholder = {
                        Text(
                            text = if (isListening) "Boss, ARIC is listening..." else "Type command for Boss...",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        focusedContainerColor = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurfaceVariant
                    ),
                    trailingIcon = {
                        if (textInput.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    val toSend = textInput
                                    textInput = ""
                                    viewModel.submitTextCommand(toSend)
                                },
                                modifier = Modifier.testTag("send_button")
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Send", tint = CyanPrimary)
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Jarvis Arc Mic FAB
                FloatingActionButton(
                    onClick = { viewModel.toggleListening() },
                    modifier = Modifier
                        .size(50.dp)
                        .testTag("mic_fab"),
                    containerColor = if (isListening) MaterialTheme.colorScheme.error else CyanPrimary,
                    contentColor = if (isListening) Color.White else Color(0xFF002530),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = if (isListening) "Stop Listening" else "Start Voice Command",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DirectiveButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = DarkSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ChatBubble(
    message: ChatMessageEntity,
    onSpeakAgain: () -> Unit
) {
    val isUser = message.sender == "user"
    val timeStr = remember(message.timestamp) {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(message.timestamp))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(0.88f),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                color = if (isUser) CyanPrimary.copy(alpha = 0.16f) else DarkSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isUser) CyanPrimary.copy(alpha = 0.35f) else DarkCardBorder.copy(alpha = 0.2f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (!isUser) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "ARIC AI",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CyanPrimary
                                )
                            )
                            if (message.actionTaken != null) {
                                Surface(
                                    color = NeonIndigo.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = message.actionTaken,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = NeonIndigo,
                                            fontSize = 9.sp
                                        )
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Text(
                        text = message.message,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = timeStr,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        )
                        if (!isUser) {
                            IconButton(onClick = onSpeakAgain, modifier = Modifier.size(20.dp)) {
                                Icon(
                                    Icons.Default.VolumeUp,
                                    contentDescription = "Speak again",
                                    tint = CyanPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
