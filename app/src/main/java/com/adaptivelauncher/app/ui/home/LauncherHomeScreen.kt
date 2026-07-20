package com.adaptivelauncher.app.ui.home

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adaptivelauncher.app.ui.drawer.AppDrawerSheet
import com.adaptivelauncher.app.ui.folders.FolderDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LauncherHomeScreen(
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showDrawer by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showFolderDialog by remember { mutableStateOf(false) }
    var selectedFolderId by remember { mutableStateOf<Long?>(null) }
    var showFolder by remember { mutableStateOf(false) }

    val homeScreenItems by viewModel.homeScreenItems.collectAsState(initial = emptyList())
    val dockItems by viewModel.dockItems.collectAsState(initial = emptyList())
    val folders by viewModel.folders.collectAsState(initial = emptyList())
    val allApps by viewModel.allApps.collectAsState(initial = emptyList())
    val currentPage by viewModel.currentPage.collectAsState()
    val pageCount by viewModel.pageCount.collectAsState()

    val pagerState = rememberPagerState(initialPage = currentPage, pageCount = { pageCount })

    LaunchedEffect(pagerState.currentPage) {
        viewModel.setCurrentPage(pagerState.currentPage)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures { change, dragAmount ->
                    change.consume()
                    if (dragAmount < -50) {
                        showDrawer = true
                    } else if (dragAmount > 50) {
                        showSearch = true
                    }
                }
            }
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { /* Dismiss menus on empty area tap */ }
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top bar with search and settings
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { showSearch = true }) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                }

                IconButton(onClick = onNavigateToSettings) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            // Home screen pages
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                val pageItems = homeScreenItems.filter { it.page == page }
                HomeScreenPage(
                    items = pageItems,
                    folders = folders,
                    onAppClick = { item ->
                        val intent = viewModel.getLaunchIntent(item.packageName, item.activityName)
                        intent?.let { context.startActivity(it) }
                        viewModel.trackLauncherLaunch(item.packageName)
                    },
                    onFolderClick = { folderId ->
                        selectedFolderId = folderId
                        showFolder = true
                    },
                    onItemLongClick = { /* TODO: context menu */ },
                    onCreateFolder = { showFolderDialog = true }
                )
            }

            // Page indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pageCount) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(if (isSelected) 10.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) Color.White
                                else Color.White.copy(alpha = 0.4f)
                            )
                    )
                }
            }

            // Dock
            if (dockItems.isNotEmpty()) {
                DockBar(
                    items = dockItems,
                    onAppClick = { item ->
                        val intent = viewModel.getLaunchIntent(item.packageName, item.activityName)
                        intent?.let { context.startActivity(it) }
                        viewModel.trackLauncherLaunch(item.packageName)
                    }
                )
            }
        }

        // App drawer button
        FloatingActionButton(
            onClick = { showDrawer = true },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
                .size(48.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            shape = CircleShape
        ) {
            Icon(
                Icons.Default.Apps,
                contentDescription = "App Drawer",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }

    // App drawer
    AppDrawerSheet(
        visible = showDrawer,
        apps = allApps,
        onDismiss = { showDrawer = false },
        onAppClick = { app ->
            val intent = viewModel.getLaunchIntent(app.packageName, app.activityName)
            intent?.let { context.startActivity(it) }
            viewModel.trackLauncherLaunch(app.packageName)
            showDrawer = false
        },
        onAddToFolder = { app ->
            showFolderDialog = true
            showDrawer = false
        },
        onAddToHomeScreen = { app ->
            viewModel.addToHomeScreen(app)
            showDrawer = false
        }
    )

    // Search overlay
    AnimatedVisibility(
        visible = showSearch,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
    ) {
        SearchOverlay(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onDismiss = {
                showSearch = false
                searchQuery = ""
            },
            apps = allApps.filter {
                it.label.contains(searchQuery, ignoreCase = true) ||
                    it.packageName.contains(searchQuery, ignoreCase = true)
            },
            onAppClick = { app ->
                val intent = viewModel.getLaunchIntent(app.packageName, app.activityName)
                intent?.let { context.startActivity(it) }
                viewModel.trackLauncherLaunch(app.packageName)
                showSearch = false
                searchQuery = ""
            }
        )
    }

    // Folder creation dialog
    if (showFolderDialog) {
        FolderDialog(
            folders = folders,
            apps = allApps,
            onDismiss = { showFolderDialog = false },
            onCreateFolder = { name ->
                viewModel.createFolder(name)
                showFolderDialog = false
            },
            onAddToFolder = { folderId, packageName ->
                viewModel.addAppToFolder(folderId, packageName)
                showFolderDialog = false
            }
        )
    }

    // Folder view dialog
    if (showFolder && selectedFolderId != null) {
        FolderViewDialog(
            folderId = selectedFolderId!!,
            onDismiss = { showFolder = false },
            viewModel = viewModel
        )
    }
}

@Composable
private fun DockBar(
    items: List<com.adaptivelauncher.app.data.db.HomeScreenItemEntity>,
    onAppClick: (com.adaptivelauncher.app.data.db.HomeScreenItemEntity) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items.take(5).forEach { item ->
            AppIcon(
                packageName = item.packageName,
                label = item.label,
                onClick = { onAppClick(item) }
            )
        }
    }
}

@Composable
private fun AppIcon(
    packageName: String,
    label: String,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            val icon = remember(packageName) {
                try {
                    context.packageManager.getApplicationIcon(packageName)
                } catch (e: Exception) {
                    null
                }
            }

            if (icon != null) {
                Image(
                    bitmap = androidx.compose.ui.graphics.asImageBitmap(
                        icon.toBitmap(48, 48)
                    ),
                    contentDescription = label,
                    modifier = Modifier.size(40.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.8f),
            maxLines = 1
        )
    }
}

@Composable
private fun SearchOverlay(
    query: String,
    onQueryChange: (String) -> Unit,
    onDismiss: () -> Unit,
    apps: List<com.adaptivelauncher.app.data.db.LauncherApplicationEntity>,
    onAppClick: (com.adaptivelauncher.app.data.db.LauncherApplicationEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f))
            .padding(16.dp)
    ) {
        SearchBar(
            query = query,
            onQueryChange = onQueryChange,
            onSearch = {},
            active = true,
            onActiveChange = {},
            placeholder = { Text("Search apps...") },
            leadingIcon = {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            },
            colors = SearchBarDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            apps.take(20).forEach { app ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAppClick(app) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppIcon(
                        packageName = app.packageName,
                        label = app.label,
                        onClick = { onAppClick(app) }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = app.label,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
