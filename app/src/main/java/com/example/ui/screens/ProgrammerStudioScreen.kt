package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.RoutineEntity
import com.example.ui.AricViewModel
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRose

@Composable
fun ProgrammerStudioScreen(
    viewModel: AricViewModel,
    modifier: Modifier = Modifier
) {
    val routines by viewModel.routines.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingRoutine by remember { mutableStateOf<RoutineEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // === Studio Header ===
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = CyanPrimary.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Code,
                                    contentDescription = "Code",
                                    tint = CyanPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ROUTINE PROGRAMMER",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CyanPrimary,
                                    letterSpacing = 1.sp
                                )
                            )
                            Text(
                                text = "Apne phone me custom voice commands banayein",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Starter Routine Templates
                Text(
                    text = "Quick Command Templates:",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        AssistChip(
                            onClick = {
                                viewModel.createOrUpdateRoutine(
                                    title = "Study Mode",
                                    triggerPhrase = "padhai shuru",
                                    actionType = "SPEAK",
                                    actionPayload = "Phone focus mode on. Padhai me dhyan lagayein!",
                                    speakResponse = "Phone focus mode on. Padhai me dhyan lagayein!"
                                )
                            },
                            label = { Text("+ Study Mode") },
                            colors = AssistChipDefaults.assistChipColors(containerColor = DarkSurfaceVariant)
                        )
                    }
                    item {
                        AssistChip(
                            onClick = {
                                viewModel.createOrUpdateRoutine(
                                    title = "Night Emergency Torch",
                                    triggerPhrase = "emergency batti",
                                    actionType = "TOGGLE_TORCH",
                                    actionPayload = "toggle",
                                    speakResponse = "Emergency torch activated."
                                )
                            },
                            label = { Text("+ SOS Torch") },
                            colors = AssistChipDefaults.assistChipColors(containerColor = DarkSurfaceVariant)
                        )
                    }
                    item {
                        AssistChip(
                            onClick = {
                                viewModel.createOrUpdateRoutine(
                                    title = "Quick YouTube Music",
                                    triggerPhrase = "lofi music",
                                    actionType = "OPEN_URL",
                                    actionPayload = "https://www.youtube.com/results?search_query=lofi+hip+hop+radio",
                                    speakResponse = "Playing Lo-Fi music on YouTube."
                                )
                            },
                            label = { Text("+ Lo-Fi Music") },
                            colors = AssistChipDefaults.assistChipColors(containerColor = DarkSurfaceVariant)
                        )
                    }
                }
            }
        }

        // === List of Programmed Commands ===
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Programmed Voice Routines (${routines.size})",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            Button(
                onClick = {
                    editingRoutine = null
                    showAddDialog = true
                },
                modifier = Modifier.testTag("new_routine_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color(0xFF002530), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Command", color = Color(0xFF002530), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (routines.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Tune,
                        contentDescription = "No routines",
                        tint = CyanPrimary.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Abhi koi custom routine nahi hai",
                        style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onSurface)
                    )
                    Text(
                        text = "Upar 'New Command' dabayein aur apne bolne par phone se jo karwana ho program karein!",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
            ) {
                items(routines) { routine ->
                    RoutineCard(
                        routine = routine,
                        onToggle = { viewModel.toggleRoutineState(routine) },
                        onTest = { viewModel.testRoutine(routine) },
                        onDelete = { viewModel.deleteRoutine(routine) },
                        onEdit = {
                            editingRoutine = routine
                            showAddDialog = true
                        }
                    )
                }
            }
        }
    }

    // Add / Edit Routine Dialog
    if (showAddDialog) {
        ProgramRoutineDialog(
            existingRoutine = editingRoutine,
            onDismiss = { showAddDialog = false },
            onSave = { title, trigger, type, payload, speakText ->
                viewModel.createOrUpdateRoutine(
                    id = editingRoutine?.id ?: 0,
                    title = title,
                    triggerPhrase = trigger,
                    actionType = type,
                    actionPayload = payload,
                    speakResponse = speakText,
                    isEnabled = true
                )
                showAddDialog = false
            }
        )
    }
}

@Composable
fun RoutineCard(
    routine: RoutineEntity,
    onToggle: () -> Unit,
    onTest: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (routine.isEnabled) DarkCardBorder.copy(alpha = 0.25f) else Color.Transparent
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val (icon, color) = getActionIconAndColor(routine.actionType)
                    Surface(
                        shape = CircleShape,
                        color = color.copy(alpha = 0.15f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = routine.title,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = "Action: ${routine.actionType}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = color,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Switch(
                    checked = routine.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CyanPrimary,
                        checkedTrackColor = CyanPrimary.copy(alpha = 0.3f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Trigger & Response info
            Surface(
                color = DarkSurfaceVariant,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Mic, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "When you say: \"${routine.triggerPhrase}\"",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = CyanPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                    if (routine.speakResponse.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ARIC speaks: \"${routine.speakResponse}\"",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                    if (routine.actionPayload.isNotBlank() && routine.actionType != "SPEAK") {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Target: ${routine.actionPayload}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom action row: Test Run and Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onTest,
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Test", tint = CyanPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Test Run", color = CyanPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = NeonRose.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

fun getActionIconAndColor(actionType: String): Pair<ImageVector, Color> {
    return when (actionType) {
        "SPEAK" -> Pair(Icons.Default.VolumeUp, NeonPurple)
        "OPEN_APP" -> Pair(Icons.Default.SmartToy, CyanPrimary)
        "OPEN_URL" -> Pair(Icons.Default.Language, NeonIndigo)
        "TOGGLE_TORCH" -> Pair(Icons.Default.FlashOn, NeonEmerald)
        "DIAL_PHONE" -> Pair(Icons.Default.Phone, Color(0xFFF59E0B))
        "CUSTOM_AI" -> Pair(Icons.Default.Psychology, NeonPurple)
        else -> Pair(Icons.Default.Code, CyanPrimary)
    }
}

@Composable
fun ProgramRoutineDialog(
    existingRoutine: RoutineEntity?,
    onDismiss: () -> Unit,
    onSave: (title: String, trigger: String, type: String, payload: String, speakText: String) -> Unit
) {
    var title by remember { mutableStateOf(existingRoutine?.title ?: "") }
    var trigger by remember { mutableStateOf(existingRoutine?.triggerPhrase ?: "") }
    var selectedType by remember { mutableStateOf(existingRoutine?.actionType ?: "SPEAK") }
    var payload by remember { mutableStateOf(existingRoutine?.actionPayload ?: "") }
    var speakText by remember { mutableStateOf(existingRoutine?.speakResponse ?: "") }

    val actionTypes = listOf(
        "SPEAK" to "Speak Custom Response",
        "OPEN_APP" to "Launch App (Camera/Calculator/etc)",
        "OPEN_URL" to "Open Website or YouTube Link",
        "TOGGLE_TORCH" to "Flashlight / Torch Toggle",
        "DIAL_PHONE" to "Dial Phone Number",
        "CUSTOM_AI" to "Custom AI Prompt"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existingRoutine == null) "Program New Voice Routine" else "Edit Voice Routine",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = CyanPrimary)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Routine Name (e.g. My Morning Plan)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("routine_title_input")
                )

                OutlinedTextField(
                    value = trigger,
                    onValueChange = { trigger = it },
                    label = { Text("Trigger Voice Phrase (Hindi/English)") },
                    placeholder = { Text("e.g. subah ho gayi ya hello aric") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("routine_trigger_input")
                )

                Text(
                    text = "Select Action Type:",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                // Action type selector
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    actionTypes.forEach { (type, label) ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedType == type) CyanPrimary.copy(alpha = 0.2f) else DarkSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (selectedType == type) CyanPrimary else Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedType = type }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val (icon, color) = getActionIconAndColor(type)
                                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (selectedType == type) CyanPrimary else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (selectedType == type) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            }
                        }
                    }
                }

                if (selectedType != "SPEAK" && selectedType != "TOGGLE_TORCH") {
                    OutlinedTextField(
                        value = payload,
                        onValueChange = { payload = it },
                        label = {
                            Text(
                                when (selectedType) {
                                    "OPEN_APP" -> "App Name (camera, calculator, youtube, etc.)"
                                    "OPEN_URL" -> "Target URL (https://...)"
                                    "DIAL_PHONE" -> "Phone Number (e.g. 9876543210)"
                                    "CUSTOM_AI" -> "Custom AI Query / Task"
                                    else -> "Action Target"
                                }
                            )
                        },
                        singleLine = selectedType != "CUSTOM_AI",
                        modifier = Modifier.fillMaxWidth().testTag("routine_payload_input")
                    )
                }

                OutlinedTextField(
                    value = speakText,
                    onValueChange = { speakText = it },
                    label = { Text("What ARIC Speaks Back (Hindi/English)") },
                    placeholder = { Text("e.g. Shubh prabhat! Sab theek hai.") },
                    modifier = Modifier.fillMaxWidth().testTag("routine_speak_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && trigger.isNotBlank()) {
                        onSave(title, trigger, selectedType, payload, speakText)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                modifier = Modifier.testTag("save_routine_button")
            ) {
                Text("Save & Program", color = Color(0xFF002530), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
