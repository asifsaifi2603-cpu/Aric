package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.data.local.MemoryEntity
import com.example.ui.AricViewModel
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRose
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MemoryVaultScreen(
    viewModel: AricViewModel,
    modifier: Modifier = Modifier
) {
    val memories by viewModel.memories.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showJsonDialog by remember { mutableStateOf(false) }

    val filteredMemories = remember(memories, searchQuery) {
        if (searchQuery.isBlank()) {
            memories
        } else {
            memories.filter { it.text.contains(searchQuery, ignoreCase = true) || it.category.contains(searchQuery, ignoreCase = true) }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // === Memory Vault Header ===
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
                            color = NeonPurple.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Psychology,
                                    contentDescription = "Memory",
                                    tint = NeonPurple,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ARIC MEMORY VAULT",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NeonPurple,
                                    letterSpacing = 1.sp
                                )
                            )
                            Text(
                                text = "Saved facts, notes & personal knowledge",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    color = DarkSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "💡 Voice Tip: Aap anytime bol sakte hain: \"ARIC, yaad rakho mera roll number 42 hai\" — ARIC automatically yahan save kar lega!",
                        style = MaterialTheme.typography.bodySmall.copy(color = CyanPrimary, fontSize = 12.sp),
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }

        // Search Bar & Add Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("memory_search_input"),
                placeholder = { Text("Search memories...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = CyanPrimary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("add_memory_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color(0xFF002530), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add", color = Color(0xFF002530), fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(6.dp))

            Button(
                onClick = { showJsonDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.4f))
            ) {
                Text("{JSON}", color = CyanPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Memory List
        if (filteredMemories.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Bookmark,
                        contentDescription = "Empty",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (searchQuery.isEmpty()) "Koi memory saved nahi hai" else "No matching memory found",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
            ) {
                items(filteredMemories) { mem ->
                    MemoryCard(
                        memory = mem,
                        onDelete = { viewModel.deleteMemory(mem) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddMemoryDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { note, category ->
                viewModel.addManualMemory(note, category)
                showAddDialog = false
            }
        )
    }

    if (showJsonDialog) {
        JsonSyncDialog(
            currentJson = viewModel.exportMemoriesJson(),
            onDismiss = { showJsonDialog = false },
            onImport = { jsonInput ->
                val importedCount = viewModel.importMemoriesFromJson(jsonInput)
                showJsonDialog = false
            }
        )
    }
}

@Composable
fun JsonSyncDialog(
    currentJson: String,
    onDismiss: () -> Unit,
    onImport: (String) -> Unit
) {
    var jsonText by remember { mutableStateOf(currentJson) }
    var importStatus by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Memory Database JSON (Import / Export)",
                color = CyanPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Aap apna Python `aric_memory.json` ya koi bhi database JSON yahan paste karke import kar sakte hain, ya current database copy kar sakte hain:",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                OutlinedTextField(
                    value = jsonText,
                    onValueChange = { jsonText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                )

                if (importStatus != null) {
                    Text(text = importStatus ?: "", color = NeonEmerald, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (jsonText.isNotBlank()) {
                        onImport(jsonText)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
            ) {
                Text("Import into ARIC", color = Color(0xFF002530), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun MemoryCard(
    memory: MemoryEntity,
    onDelete: () -> Unit
) {
    val dateStr = remember(memory.timestamp) {
        SimpleDateFormat("d MMM, hh:mm a", Locale.getDefault()).format(Date(memory.timestamp))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = NeonIndigo.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = memory.category,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(color = NeonIndigo, fontSize = 10.sp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = memory.text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete Memory",
                    tint = NeonRose.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun AddMemoryDialog(
    onDismiss: () -> Unit,
    onAdd: (note: String, category: String) -> Unit
) {
    var note by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Personal") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nayi Memory Save Karein", color = CyanPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Memory Fact / Note") },
                    placeholder = { Text("e.g. WiFi password is ...") },
                    modifier = Modifier.fillMaxWidth().testTag("memory_note_input")
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (Personal, Work, Important)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (note.isNotBlank()) {
                        onAdd(note, category)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                modifier = Modifier.testTag("confirm_add_memory")
            ) {
                Text("Save Memory", color = Color(0xFF002530), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
