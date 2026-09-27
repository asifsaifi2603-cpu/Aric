package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AricViewModel
import com.example.ui.screens.AricHomeScreen
import com.example.ui.screens.DeviceToolsScreen
import com.example.ui.screens.MemoryVaultScreen
import com.example.ui.screens.ProgrammerStudioScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.MyApplicationTheme

enum class AricScreen(val title: String, val icon: ImageVector) {
    HUD("ARIC", Icons.Default.Mic),
    PROGRAMMER("Programmer", Icons.Default.Code),
    MEMORY("Memory", Icons.Default.Psychology),
    TOOLS("Tools", Icons.Default.Smartphone),
    SETTINGS("Settings", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val viewModel: AricViewModel = viewModel()
                AricAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AricAppContent(viewModel: AricViewModel) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf(AricScreen.HUD) }

    // Request Audio & Camera permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Graceful handling of permissions
    }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.CAMERA)
        }
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    // Handle back button on secondary screens
    if (currentScreen != AricScreen.HUD) {
        BackHandler {
            currentScreen = AricScreen.HUD
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                AricScreen.entries.forEach { screen ->
                    val isSelected = currentScreen == screen
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentScreen = screen },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title
                            )
                        },
                        label = { Text(screen.title) },
                        modifier = Modifier.testTag("nav_item_${screen.name.lowercase()}"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyanPrimary,
                            selectedTextColor = CyanPrimary,
                            indicatorColor = CyanPrimary.copy(alpha = 0.2f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AricScreen.HUD -> AricHomeScreen(
                    viewModel = viewModel,
                    onNavigateToProgrammer = { currentScreen = AricScreen.PROGRAMMER }
                )
                AricScreen.PROGRAMMER -> ProgrammerStudioScreen(
                    viewModel = viewModel
                )
                AricScreen.MEMORY -> MemoryVaultScreen(
                    viewModel = viewModel
                )
                AricScreen.TOOLS -> DeviceToolsScreen(
                    viewModel = viewModel
                )
                AricScreen.SETTINGS -> SettingsScreen(
                    viewModel = viewModel
                )
            }
        }
    }
}
