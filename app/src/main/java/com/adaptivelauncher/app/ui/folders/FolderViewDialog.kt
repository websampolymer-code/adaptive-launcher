package com.adaptivelauncher.app.ui.folders

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.adaptivelauncher.app.LauncherApp
import com.adaptivelauncher.app.data.db.FolderEntity
import com.adaptivelauncher.app.ui.home.HomeViewModel
import kotlinx.coroutines.launch

@Composable
fun FolderViewDialog(
    folderId: Long,
    onDismiss: () -> Unit,
    viewModel: HomeViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = LauncherApp.instance.database

    var showContextMenu by remember { mutableStateOf<String?>(null) }

    val folders by viewModel.folders.collectAsState(initial = emptyList())
    val folder = folders.find { it.id == folderId }

    val folderApps by remember(folderId) {
        database.folderDao().getFolderApps(folderId)
    }.collectAsState(initial = emptyList())

    val allApps by viewModel.allApps.collectAsState(initial = emptyList())

    if (folder == null) {
        onDismiss()
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = folder.name,
                    style = MaterialTheme.typography.headlineSmall
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (folder.smartModeEnabled) {
                        Icon(
                            Icons.Default.SwapHoriz,
                            contentDescription = "Smart mode",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            }
        },
        text = {
            Column {
                // Smart mode toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Smart mode", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Auto-sort by usage",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    Switch(
                        checked = folder.smartModeEnabled,
                        onCheckedChange = { enabled ->
                            scope.launch {
                                val updated = folder.copy(
                                    smartModeEnabled = enabled,
                                    sortMode = if (enabled) com.adaptivelauncher.app.data.db.SortMode.SMART else com.adaptivelauncher.app.data.db.SortMode.MANUAL
                                )
                                database.folderDao().updateFolder(updated)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (folderApps.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Empty folder",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier.height(300.dp),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(folderApps) { folderApp ->
                            val appInfo = allApps.find { it.packageName == folderApp.packageName }
                            val label = appInfo?.label ?: folderApp.packageName.substringAfterLast('.')

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .combinedClickable(
                                        onClick = {
                                            val intent = viewModel.getLaunchIntent(folderApp.packageName, null)
                                            intent?.let { context.startActivity(it) }
                                        },
                                        onLongClick = {
                                            showContextMenu = folderApp.packageName
                                        },
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    )
                                    .padding(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val icon = remember(folderApp.packageName) {
                                        getAdaptiveIcon(context, folderApp.packageName)
                                    }

                                    if (icon != null) {
                                        Image(
                                            bitmap = icon.asImageBitmap(),
                                            contentDescription = label,
                                            modifier = Modifier.size(40.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    } else {
                                        Text(
                                            label.take(1).uppercase(),
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )

                                if (folderApp.isPinned) {
                                    Text(
                                        text = "Pinned",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )

    // Context menu for long-press
    showContextMenu?.let { packageName ->
        DropdownMenu(
            expanded = true,
            onDismissRequest = { showContextMenu = null }
        ) {
            DropdownMenuItem(
                text = { Text("Pin to position") },
                onClick = {
                    scope.launch {
                        database.folderDao().getFolderAppsList(folderId).let { apps ->
                            val app = apps.find { it.packageName == packageName }
                            app?.let {
                                database.folderDao().updateFolderApp(
                                    it.copy(isPinned = true, pinnedPosition = it.manualPosition)
                                )
                            }
                        }
                    }
                    showContextMenu = null
                }
            )
            DropdownMenuItem(
                text = { Text("Remove from folder") },
                onClick = {
                    scope.launch {
                        database.folderDao().deleteFolderAppByPackage(folderId, packageName)
                    }
                    showContextMenu = null
                }
            )
            DropdownMenuItem(
                text = { Text("App info") },
                onClick = {
                    val intent = viewModel.getAppInfo(packageName)
                    context.startActivity(intent)
                    showContextMenu = null
                }
            )
            DropdownMenuItem(
                text = { Text("Exclude from ranking") },
                onClick = {
                    scope.launch {
                        database.folderDao().getFolderAppsList(folderId).let { apps ->
                            val app = apps.find { it.packageName == packageName }
                            app?.let {
                                database.folderDao().updateFolderApp(
                                    it.copy(excludedFromRanking = !it.excludedFromRanking)
                                )
                            }
                        }
                    }
                    showContextMenu = null
                }
            )
        }
    }
}

private fun getAdaptiveIcon(context: Context, packageName: String): Bitmap? {
    return try {
        val drawable = context.packageManager.getApplicationIcon(packageName)
        if (drawable is AdaptiveIconDrawable) {
            val bitmap = Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, 96, 96)
            drawable.draw(canvas)
            bitmap
        } else {
            val bitmap = Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, 48, 48)
            drawable.draw(canvas)
            bitmap
        }
    } catch (e: Exception) {
        null
    }
}
