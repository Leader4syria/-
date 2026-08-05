package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.data.models.PlannedSessionEntity
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodels.MasarViewModel
import com.example.ui.viewmodels.OnboardingViewModel

sealed class Screen(val route: String, val title: String, val activeIcon: ImageVector, val inactiveIcon: ImageVector) {
    object Onboarding : Screen("onboarding", "الإعداد", Icons.Filled.Start, Icons.Outlined.Start)
    object Today : Screen("today", "اليوم", Icons.Filled.Today, Icons.Outlined.Today)
    object Plan : Screen("plan", "الخطة", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth)
    object Subjects : Screen("subjects", "المواد", Icons.Filled.MenuBook, Icons.Outlined.MenuBook)
    object Exams : Screen("exams", "الامتحانات", Icons.Filled.Assignment, Icons.Outlined.Assignment)
    object Progress : Screen("progress", "التقدم", Icons.Filled.Analytics, Icons.Outlined.Analytics)
    object Settings : Screen("settings", "الإعدادات", Icons.Filled.Settings, Icons.Outlined.Settings)
    object FocusSession : Screen("focus_session", "جلسة التركيز", Icons.Filled.Timer, Icons.Outlined.Timer)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasarNavigation(
    mainViewModel: MasarViewModel,
    onboardingViewModel: OnboardingViewModel
) {
    val navController = rememberNavController()
    val profile by mainViewModel.profileFlow.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        Screen.Today,
        Screen.Plan,
        Screen.Subjects,
        Screen.Exams,
        Screen.Progress
    )

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }
    val showTopBar = showBottomBar

    val startDestination = if (profile?.isOnboardingCompleted == true) Screen.Today.route else Screen.Onboarding.route

    var showExitDialog by remember { mutableStateOf(false) }

    if (currentRoute == Screen.Today.route) {
        BackHandler {
            showExitDialog = true
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(PrimaryIndigo.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = null,
                        tint = PrimaryIndigo,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            title = {
                Text(
                    "تأكيد الخروج",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    "هل تريد الخروج من التطبيق الآن؟",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitDialog = false
                        (navController.context as? android.app.Activity)?.finish()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = CircleShape
                ) {
                    Text("خروج", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showExitDialog = false },
                    shape = CircleShape
                ) {
                    Text("إلغاء", fontWeight = FontWeight.Medium)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    Scaffold(
        topBar = {
            if (showTopBar) {
                CenterAlignedTopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(PrimaryIndigo, PrimaryTeal)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "مَســار",
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp,
                                letterSpacing = 0.5.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { navController.navigate(Screen.Settings.route) },
                            modifier = Modifier.testTag("settings_top_button")
                        ) {
                            Icon(
                                imageVector = if (currentRoute == Screen.Settings.route) Icons.Filled.Settings else Icons.Outlined.Settings,
                                contentDescription = "الإعدادات",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp)
                            .shadow(
                                elevation = 12.dp,
                                shape = CircleShape,
                                ambientColor = PrimaryIndigo.copy(alpha = 0.2f),
                                spotColor = PrimaryIndigo.copy(alpha = 0.3f)
                            ),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // First 2 navigation items
                            bottomNavItems.take(2).forEach { screen ->
                                val selected = currentRoute == screen.route
                                FloatingNavItem(
                                    screen = screen,
                                    selected = selected,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }

                            // Center Prominent Floating Action Button (+) matching reference image
                            Surface(
                                modifier = Modifier
                                    .size(52.dp)
                                    .testTag("center_action_button"),
                                shape = CircleShape,
                                color = PrimaryIndigo,
                                shadowElevation = 6.dp
                            ) {
                                IconButton(
                                    onClick = {
                                        navController.navigate(Screen.Today.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "بدء تركيز / إضافة",
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            // Last 2 navigation items
                            bottomNavItems.drop(2).take(2).forEach { screen ->
                                val selected = currentRoute == screen.route
                                FloatingNavItem(
                                    screen = screen,
                                    selected = selected,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        var activeFocusSession by remember { mutableStateOf<PlannedSessionEntity?>(null) }

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(androidx.compose.animation.core.tween(220, easing = androidx.compose.animation.core.FastOutSlowInEasing)) + slideInHorizontally(initialOffsetX = { 60 }, animationSpec = androidx.compose.animation.core.tween(220)) },
            exitTransition = { fadeOut(androidx.compose.animation.core.tween(220, easing = androidx.compose.animation.core.FastOutSlowInEasing)) + slideOutHorizontally(targetOffsetX = { -60 }, animationSpec = androidx.compose.animation.core.tween(220)) },
            popEnterTransition = { fadeIn(androidx.compose.animation.core.tween(220, easing = androidx.compose.animation.core.FastOutSlowInEasing)) + slideInHorizontally(initialOffsetX = { -60 }, animationSpec = androidx.compose.animation.core.tween(220)) },
            popExitTransition = { fadeOut(androidx.compose.animation.core.tween(220, easing = androidx.compose.animation.core.FastOutSlowInEasing)) + slideOutHorizontally(targetOffsetX = { 60 }, animationSpec = androidx.compose.animation.core.tween(220)) }
        ) {
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    viewModel = onboardingViewModel,
                    onFinished = {
                        navController.navigate(Screen.Today.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Today.route) {
                TodayScreen(
                    viewModel = mainViewModel,
                    onStartFocus = { session ->
                        activeFocusSession = session
                        navController.navigate(Screen.FocusSession.route)
                    },
                    onNavigateToPlan = {
                        navController.navigate(Screen.Plan.route)
                    }
                )
            }


            composable(Screen.Plan.route) {
                PlanScreen(viewModel = mainViewModel)
            }

            composable(Screen.Subjects.route) {
                SubjectsScreen(viewModel = mainViewModel)
            }

            composable(Screen.Exams.route) {
                ExamsScreen(viewModel = mainViewModel)
            }

            composable(Screen.Progress.route) {
                ProgressScreen(viewModel = mainViewModel)
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = mainViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.FocusSession.route) {
                val session = activeFocusSession
                if (session != null) {
                    FocusSessionScreen(
                        session = session,
                        viewModel = mainViewModel,
                        onBack = { navController.popBackStack() }
                    )
                } else {
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                }
            }
        }
    }
}

@Composable
private fun FloatingNavItem(
    screen: Screen,
    selected: Boolean,
    onClick: () -> Unit
) {
    val activeColor = PrimaryIndigo
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant

    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (selected) 1.15f else 1.0f,
        animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.6f),
        label = "NavItemScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("nav_${screen.route}")
    ) {
        Icon(
            imageVector = if (selected) screen.activeIcon else screen.inactiveIcon,
            contentDescription = screen.title,
            tint = if (selected) activeColor else inactiveColor,
            modifier = Modifier
                .size(22.dp)
                .scale(scale)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = screen.title,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) activeColor else inactiveColor
        )
    }
}


