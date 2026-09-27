package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AricViewModel
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.NeonPurple

@Composable
fun DeviceToolsScreen(
    viewModel: AricViewModel,
    modifier: Modifier = Modifier
) {
    var quickSearch by remember { mutableStateOf("") }
    var quickPhone by remember { mutableStateOf("") }
    var actionStatus by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // === Device Tools Header ===
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = NeonEmerald.copy(alpha = 0.2f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Smartphone, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "DEVICE CONTROL CENTER",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = NeonEmerald,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = "Hardware shortcuts and Android phone automation",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }

                if (actionStatus != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = CyanPrimary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = actionStatus ?: "",
                            style = MaterialTheme.typography.bodySmall.copy(color = CyanPrimary),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Grid of direct hardware/phone actions
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            item {
                ToolButtonCard(
                    title = "Flashlight / Torch",
                    subtitle = "Toggle LED light",
                    icon = Icons.Default.FlashOn,
                    iconColor = Color(0xFFF59E0B),
                    onClick = {
                        val res = viewModel.deviceActionHandler.toggleTorch()
                        actionStatus = if (res.isSuccess) "Flashlight toggled!" else "Flashlight toggle failed"
                    }
                )
            }
            item {
                ToolButtonCard(
                    title = "Open Camera",
                    subtitle = "Photo / Video capture",
                    icon = Icons.Default.CameraAlt,
                    iconColor = CyanPrimary,
                    onClick = {
                        viewModel.deviceActionHandler.openCamera()
                        actionStatus = "Camera launched"
                    }
                )
            }
            item {
                ToolButtonCard(
                    title = "Calculator",
                    subtitle = "Quick calculation tool",
                    icon = Icons.Default.Calculate,
                    iconColor = NeonPurple,
                    onClick = {
                        viewModel.deviceActionHandler.openCalculator()
                        actionStatus = "Calculator opened"
                    }
                )
            }
            item {
                ToolButtonCard(
                    title = "YouTube",
                    subtitle = "Open video & music",
                    icon = Icons.Default.PlayCircle,
                    iconColor = Color(0xFFEF4444),
                    onClick = {
                        viewModel.deviceActionHandler.openYouTube()
                        actionStatus = "YouTube launched"
                    }
                )
            }
            item {
                ToolButtonCard(
                    title = "Wi-Fi Settings",
                    subtitle = "Manage network connections",
                    icon = Icons.Default.Wifi,
                    iconColor = NeonEmerald,
                    onClick = {
                        viewModel.deviceActionHandler.openWifiSettings()
                        actionStatus = "Wi-Fi settings opened"
                    }
                )
            }
            item {
                ToolButtonCard(
                    title = "Bluetooth",
                    subtitle = "Paired devices & sync",
                    icon = Icons.Default.Bluetooth,
                    iconColor = NeonIndigo,
                    onClick = {
                        viewModel.deviceActionHandler.openBluetoothSettings()
                        actionStatus = "Bluetooth settings opened"
                    }
                )
            }
            item {
                ToolButtonCard(
                    title = "Battery Saver",
                    subtitle = "Power management",
                    icon = Icons.Default.BatteryChargingFull,
                    iconColor = NeonEmerald,
                    onClick = {
                        viewModel.deviceActionHandler.openBatterySettings()
                        actionStatus = "Battery settings opened"
                    }
                )
            }
            item {
                ToolButtonCard(
                    title = "Phone Settings",
                    subtitle = "System control panel",
                    icon = Icons.Default.Settings,
                    iconColor = Color(0xFF94A3B8),
                    onClick = {
                        viewModel.deviceActionHandler.openSystemSettings()
                        actionStatus = "System settings opened"
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Dialer & Search Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Quick Search & Phone Actions",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = CyanPrimary)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = quickSearch,
                        onValueChange = { quickSearch = it },
                        placeholder = { Text("Search Google or YouTube...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = {
                            if (quickSearch.isNotBlank()) {
                                viewModel.deviceActionHandler.openGoogleSearch(quickSearch)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Language, contentDescription = "Search", tint = Color(0xFF002530))
                    }
                }
            }
        }
    }
}

@Composable
fun ToolButtonCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = iconColor.copy(alpha = 0.15f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
                }
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}
