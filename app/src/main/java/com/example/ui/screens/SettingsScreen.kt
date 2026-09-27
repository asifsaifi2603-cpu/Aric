package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.AricSettings
import com.example.ui.AricViewModel
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonRose

@Composable
fun SettingsScreen(
    viewModel: AricViewModel,
    modifier: Modifier = Modifier
) {
    val currentSettings by viewModel.settings.collectAsState()

    var assistantName by remember(currentSettings) { mutableStateOf(currentSettings.assistantName) }
    var ownerName by remember(currentSettings) { mutableStateOf(currentSettings.ownerName) }
    var language by remember(currentSettings) { mutableStateOf(currentSettings.language) }
    var speechRate by remember(currentSettings) { mutableFloatStateOf(currentSettings.speechRate) }
    var speechPitch by remember(currentSettings) { mutableFloatStateOf(currentSettings.speechPitch) }
    var systemPrompt by remember(currentSettings) { mutableStateOf(currentSettings.customSystemPrompt) }
    var apiKey by remember(currentSettings) { mutableStateOf(currentSettings.apiKey) }
    var saveFeedback by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // === Persona & Identity Card ===
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "AI Identity & Owner",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = CyanPrimary)
                )

                OutlinedTextField(
                    value = assistantName,
                    onValueChange = { assistantName = it },
                    label = { Text("Assistant Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("assistant_name_input")
                )

                OutlinedTextField(
                    value = ownerName,
                    onValueChange = { ownerName = it },
                    label = { Text("Owner / User Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("owner_name_input")
                )

                Text(
                    text = "Preferred Language:",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Hinglish", "Hindi", "English").forEach { lang ->
                        FilterChip(
                            selected = language == lang,
                            onClick = { language = lang },
                            label = { Text(lang) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanPrimary.copy(alpha = 0.2f),
                                selectedLabelColor = CyanPrimary
                            )
                        )
                    }
                }
            }
        }

        // === Speech & TTS Voice Tuning ===
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Voice Speech Tuning",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = CyanPrimary)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Speech Rate (${String.format("%.1fx", speechRate)})", style = MaterialTheme.typography.bodyMedium)
                }
                Slider(
                    value = speechRate,
                    onValueChange = { speechRate = it },
                    valueRange = 0.6f..1.6f,
                    colors = SliderDefaults.colors(thumbColor = CyanPrimary, activeTrackColor = CyanPrimary)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Voice Pitch (${String.format("%.1fx", speechPitch)})", style = MaterialTheme.typography.bodyMedium)
                }
                Slider(
                    value = speechPitch,
                    onValueChange = { speechPitch = it },
                    valueRange = 0.6f..1.4f,
                    colors = SliderDefaults.colors(thumbColor = CyanPrimary, activeTrackColor = CyanPrimary)
                )

                Button(
                    onClick = {
                        viewModel.voiceManager.speak(
                            text = "Namaste $ownerName! Main $assistantName hoon. Voice speech check successful.",
                            rate = speechRate,
                            pitch = speechPitch
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary.copy(alpha = 0.2f))
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Test", tint = CyanPrimary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Test Voice Speech", color = CyanPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }

        // === AI Brain & System Instructions ===
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "AI System Prompt & Brain Config",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = CyanPrimary)
                )

                Text(
                    text = "Custom Prompt (ARIC's personality, rules, and how it responds):",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                OutlinedTextField(
                    value = systemPrompt,
                    onValueChange = { systemPrompt = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6
                )

                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("Gemini API Key (Optional override)") },
                    placeholder = { Text("AI Studio automatically injects key") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Save Settings Button
        Button(
            onClick = {
                viewModel.saveSettings(
                    AricSettings(
                        assistantName = assistantName.trim(),
                        ownerName = ownerName.trim(),
                        language = language,
                        speechRate = speechRate,
                        speechPitch = speechPitch,
                        customSystemPrompt = systemPrompt.trim(),
                        apiKey = apiKey.trim()
                    )
                )
                saveFeedback = true
            },
            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("save_settings_button")
        ) {
            Icon(Icons.Default.Save, contentDescription = "Save", tint = Color(0xFF002530))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save All Settings", color = Color(0xFF002530), fontWeight = FontWeight.Bold)
        }

        if (saveFeedback) {
            Text(
                text = "✓ Settings successfully updated!",
                color = NeonEmerald,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        // === Data & Storage Reset ===
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "History & Memory Maintenance",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = NeonRose)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.clearChatHistory() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = NeonRose, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear Chat", color = NeonRose, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { viewModel.clearAllMemories() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = NeonRose, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear Memories", color = NeonRose, fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
