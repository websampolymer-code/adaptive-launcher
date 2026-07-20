package com.adaptivelauncher.app.ui.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.adaptivelauncher.app.LauncherApp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = LauncherApp.instance.preferencesManager

    val gridColumns by prefs.gridColumns.collectAsState(initial = 4)
    val gridRows by prefs.gridRows.collectAsState(initial = 5)
    val themeMode by prefs.themeMode.collectAsState(initial = "system")
    val dockEnabled by prefs.dockEnabled.collectAsState(initial = true)
    val reducedMotion by prefs.reducedMotion.collectAsState(initial = false)

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Home screen section
            Text(
                text = "Home screen",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            ListItem(
                headlineContent = { Text("Grid columns") },
                supportingContent = { Text("$gridColumns columns") },
                trailingContent = {
                    Slider(
                        value = gridColumns.toFloat(),
                        onValueChange = { scope.launch { prefs.setGridColumns(it.toInt()) } },
                        valueRange = 3f..6f,
                        steps = 2,
                        modifier = Modifier.width(120.dp)
                    )
                }
            )

            ListItem(
                headlineContent = { Text("Grid rows") },
                supportingContent = { Text("$gridRows rows") },
                trailingContent = {
                    Slider(
                        value = gridRows.toFloat(),
                        onValueChange = { scope.launch { prefs.setGridRows(it.toInt()) } },
                        valueRange = 3f..7f,
                        steps = 3,
                        modifier = Modifier.width(120.dp)
                    )
                }
            )

            ListItem(
                headlineContent = { Text("Dock") },
                supportingContent = { Text("Show dock at bottom") },
                trailingContent = {
                    Switch(
                        checked = dockEnabled,
                        onCheckedChange = { scope.launch { prefs.setDockEnabled(it) } }
                    )
                }
            )

            // Theme section
            Text(
                text = "Appearance",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            ThemeOption(
                label = "System theme",
                selected = themeMode == "system",
                onClick = { scope.launch { prefs.setThemeMode("system") } }
            )
            ThemeOption(
                label = "Light",
                selected = themeMode == "light",
                onClick = { scope.launch { prefs.setThemeMode("light") } }
            )
            ThemeOption(
                label = "Dark",
                selected = themeMode == "dark",
                onClick = { scope.launch { prefs.setThemeMode("dark") } }
            )

            ListItem(
                headlineContent = { Text("Reduced motion") },
                supportingContent = { Text("Minimize animations") },
                trailingContent = {
                    Switch(
                        checked = reducedMotion,
                        onCheckedChange = { scope.launch { prefs.setReducedMotion(it) } }
                    )
                }
            )

            // Permissions section
            Text(
                text = "Permissions",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            ListItem(
                headlineContent = { Text("Usage Access") },
                supportingContent = { Text("Required for smart folder sorting") },
                modifier = Modifier.clickable {
                    try {
                        context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                    } catch (e: Exception) {
                        context.startActivity(Intent(Settings.ACTION_SETTINGS))
                    }
                }
            )

            ListItem(
                headlineContent = { Text("Default home app") },
                supportingContent = { Text("Set Adaptive Launcher as default") },
                modifier = Modifier.clickable {
                    try {
                        context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
                    } catch (e: Exception) {
                        context.startActivity(Intent(Settings.ACTION_SETTINGS))
                    }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ThemeOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(label) },
        leadingContent = {
            if (selected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}
