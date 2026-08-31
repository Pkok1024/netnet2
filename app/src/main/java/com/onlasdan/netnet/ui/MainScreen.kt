package com.onlasdan.netnet.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.onlasdan.netnet.ui.navigation.Screen
import com.onlasdan.netnet.ui.screens.DashboardScreen
import com.onlasdan.netnet.ui.screens.DataUsageScreen
import com.onlasdan.netnet.ui.screens.NetworkDiagnosticsScreen
import com.onlasdan.netnet.ui.screens.SettingsScreen
import com.onlasdan.netnet.ui.theme.AmberWarning
import com.onlasdan.netnet.ui.theme.AppTheme
import com.onlasdan.netnet.ui.theme.CyanGlow
import com.onlasdan.netnet.ui.theme.CyanPrimary
import com.onlasdan.netnet.ui.theme.EmeraldSuccess
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = AppTheme.colors
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    // Handle back button when drawer is open
    BackHandler(enabled = drawerState.isOpen) {
        coroutineScope.launch {
            drawerState.close()
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Dashboard.route

    val currentScreen = when (currentRoute) {
        Screen.DataUsage.route -> Screen.DataUsage
        Screen.Diagnostics.route -> Screen.Diagnostics
        else -> Screen.Dashboard
    }

    val snapshot by viewModel.snapshot.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val isRunning by viewModel.isServiceRunning.collectAsStateWithLifecycle()
    val isPaused by viewModel.isPaused.collectAsStateWithLifecycle()
    val isTestingSpeed by viewModel.isTestingSpeed.collectAsStateWithLifecycle()
    val isFloatingBubbleActive by viewModel.isFloatingBubbleActive.collectAsStateWithLifecycle()
    val processUsage by viewModel.processUsage.collectAsStateWithLifecycle()
    val networkDiagnostics by viewModel.networkDiagnostics.collectAsStateWithLifecycle()
    val pingState by viewModel.pingDiagnosticState.collectAsStateWithLifecycle()
    val oneTimeDiagnosticState by viewModel.oneTimeDiagnosticState.collectAsStateWithLifecycle()

    // Permission handling for POST_NOTIFICATIONS (Android 13+)
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted ||
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
            try {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } catch (_: Throwable) {}
        }
    }

    val requestNotificationPermission = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } catch (_: Throwable) {}
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = colors.surfaceElevated,
                drawerContentColor = colors.textPrimary,
                drawerTonalElevation = 6.dp,
                modifier = Modifier
                    .widthIn(max = 380.dp)
                    .fillMaxHeight()
            ) {
                // Drawer Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(colors.accentPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = colors.accentGlow,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Settings",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Preferences & System Integrations",
                                    fontSize = 11.sp,
                                    color = colors.textSecondary
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    drawerState.close()
                                }
                            },
                            modifier = Modifier.testTag("drawer_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Settings",
                                tint = colors.textSecondary
                            )
                        }
                    }

                    HorizontalDivider(
                        color = colors.cardBorder.copy(alpha = 0.6f),
                        thickness = 1.dp
                    )
                }

                // Settings Screen scrollable inside the Drawer
                SettingsScreen(
                    settings = settings,
                    onUpdateSettings = { viewModel.updateSettings(it) },
                    onResetTodayUsage = { viewModel.resetTodayUsage() },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    ) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = colors.background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(colors.accentPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = currentScreen.selectedIcon,
                                    contentDescription = null,
                                    tint = colors.accentGlow,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = currentScreen.title,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val statusColor = if (isRunning && !isPaused) EmeraldSuccess else AmberWarning
                                    val statusText = if (!isRunning) "Service Stopped" else if (isPaused) "Service Paused" else "Promoted Active"
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(statusColor, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = statusText,
                                        fontSize = 11.sp,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                        }
                    },
                    actions = {
                        // Quick Burst Test button on top bar
                        IconButton(
                            onClick = { viewModel.runSpeedBurstTest() },
                            enabled = !isTestingSpeed,
                            modifier = Modifier.testTag("topbar_burst_button")
                        ) {
                            if (isTestingSpeed) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = AmberWarning,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = "Burst Test",
                                    tint = AmberWarning
                                )
                            }
                        }

                        // Reset Session
                        IconButton(
                            onClick = { viewModel.resetSession() },
                            modifier = Modifier.testTag("reset_session_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset Session",
                                tint = colors.textSecondary
                            )
                        }

                        // Settings Drawer Trigger Button on Top Bar Right
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    drawerState.open()
                                }
                            },
                            modifier = Modifier.testTag("topbar_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = "Open Settings",
                                tint = colors.accentGlow
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colors.background
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Content NavHost with silky smooth directional page transitions
                NavHost(
                    navController = navController,
                    startDestination = Screen.Dashboard.route,
                    enterTransition = {
                        val initialRoute = initialState.destination.route
                        val targetRoute = targetState.destination.route
                        val initialIndex = when (initialRoute) {
                            Screen.DataUsage.route -> 1
                            Screen.Diagnostics.route -> 2
                            else -> 0
                        }
                        val targetIndex = when (targetRoute) {
                            Screen.DataUsage.route -> 1
                            Screen.Diagnostics.route -> 2
                            else -> 0
                        }
                        val direction = if (targetIndex >= initialIndex) 1 else -1
                        slideInHorizontally(
                            initialOffsetX = { fullWidth -> direction * (fullWidth * 0.18f).toInt() },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ) + fadeIn(animationSpec = tween(280, easing = FastOutSlowInEasing)) +
                        scaleIn(initialScale = 0.96f, animationSpec = tween(280, easing = FastOutSlowInEasing))
                    },
                    exitTransition = {
                        val initialRoute = initialState.destination.route
                        val targetRoute = targetState.destination.route
                        val initialIndex = when (initialRoute) {
                            Screen.DataUsage.route -> 1
                            Screen.Diagnostics.route -> 2
                            else -> 0
                        }
                        val targetIndex = when (targetRoute) {
                            Screen.DataUsage.route -> 1
                            Screen.Diagnostics.route -> 2
                            else -> 0
                        }
                        val direction = if (targetIndex >= initialIndex) -1 else 1
                        slideOutHorizontally(
                            targetOffsetX = { fullWidth -> direction * (fullWidth * 0.18f).toInt() },
                            animationSpec = tween(220, easing = FastOutSlowInEasing)
                        ) + fadeOut(animationSpec = tween(200, easing = FastOutSlowInEasing)) +
                        scaleOut(targetScale = 0.97f, animationSpec = tween(200, easing = FastOutSlowInEasing))
                    },
                    popEnterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { fullWidth -> -(fullWidth * 0.18f).toInt() },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ) + fadeIn(animationSpec = tween(280, easing = FastOutSlowInEasing)) +
                        scaleIn(initialScale = 0.96f, animationSpec = tween(280, easing = FastOutSlowInEasing))
                    },
                    popExitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { fullWidth -> (fullWidth * 0.18f).toInt() },
                            animationSpec = tween(220, easing = FastOutSlowInEasing)
                        ) + fadeOut(animationSpec = tween(200, easing = FastOutSlowInEasing)) +
                        scaleOut(targetScale = 0.97f, animationSpec = tween(200, easing = FastOutSlowInEasing))
                    },
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable(route = Screen.Dashboard.route) {
                        DashboardScreen(
                            snapshot = snapshot,
                            history = history,
                            settings = settings,
                            isRunning = isRunning,
                            isPaused = isPaused,
                            isTestingSpeed = isTestingSpeed,
                            isBubbleActive = isFloatingBubbleActive,
                            hasNotificationPermission = hasNotificationPermission,
                            onRequestNotificationPermission = requestNotificationPermission,
                            onToggleService = { viewModel.toggleService() },
                            onTogglePause = { viewModel.togglePause() },
                            onRunSpeedTest = { viewModel.runSpeedBurstTest() },
                            onToggleBubble = { viewModel.toggleFloatingBubble() }
                        )
                    }

                    composable(route = Screen.DataUsage.route) {
                        DataUsageScreen(
                            snapshot = snapshot,
                            speedUnit = settings.speedUnit,
                            onResetTodayUsage = { viewModel.resetTodayUsage() }
                        )
                    }

                    composable(route = Screen.Diagnostics.route) {
                        NetworkDiagnosticsScreen(
                            snapshot = snapshot,
                            diagnostics = networkDiagnostics,
                            pingState = pingState,
                            oneTimeDiagnosticState = oneTimeDiagnosticState,
                            speedUnit = settings.speedUnit,
                            resourceUsage = processUsage,
                            isServiceRunning = isRunning && !isPaused,
                            onRunPingDiagnostic = { viewModel.runPingDiagnostic() },
                            onCancelPingDiagnostic = { viewModel.cancelPingDiagnostic() },
                            onRunOneTimeDiagnostic = { viewModel.runOneTimeDiagnostic() },
                            onCancelOneTimeDiagnostic = { viewModel.cancelOneTimeDiagnostic() }
                        )
                    }
                }

                // True Floating Pill Navigation Bar (Like a FAB, completely detached and floating)
                FloatingPillNavigationBar(
                    currentRoute = currentRoute,
                    onNavigate = { screen ->
                        if (currentRoute != screen.route) {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 18.dp)
                )
            }
        }
    }
}

/**
 * Floating capsule / pill navigation bar with smooth animated item selection.
 * Floats like a FAB directly over the screen content with no enclosing background bar.
 */
@Composable
private fun FloatingPillNavigationBar(
    currentRoute: String,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors

    Surface(
        shape = CircleShape,
        color = colors.surfaceElevated.copy(alpha = 0.96f),
        tonalElevation = 10.dp,
        shadowElevation = 14.dp,
        border = BorderStroke(1.2.dp, colors.cardBorder.copy(alpha = 0.8f)),
        modifier = modifier
            .padding(horizontal = 24.dp)
            .widthIn(max = 420.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Screen.bottomNavItems.forEach { screen ->
                val isSelected = currentRoute == screen.route
                val animatedBgColor by animateColorAsState(
                    targetValue = if (isSelected) colors.accentPrimary.copy(alpha = 0.20f) else Color.Transparent,
                    animationSpec = tween(250),
                    label = "pill_bg_color"
                )
                val animatedContentColor by animateColorAsState(
                    targetValue = if (isSelected) colors.accentGlow else colors.textSecondary,
                    animationSpec = tween(250),
                    label = "pill_content_color"
                )

                Row(
                    modifier = Modifier
                        .testTag(screen.testTag)
                        .clip(CircleShape)
                        .background(animatedBgColor)
                        .clickable {
                            onNavigate(screen)
                        }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .animateContentSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                        contentDescription = screen.title,
                        tint = animatedContentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    AnimatedVisibility(
                        visible = isSelected,
                        enter = fadeIn(animationSpec = tween(200)),
                        exit = fadeOut(animationSpec = tween(150))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = screen.title,
                                color = animatedContentColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
