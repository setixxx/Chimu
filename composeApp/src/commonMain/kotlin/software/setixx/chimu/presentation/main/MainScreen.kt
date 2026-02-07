package software.setixx.chimu.presentation.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import software.setixx.chimu.domain.model.GameJam
import software.setixx.chimu.domain.model.GameJamStatus
import software.setixx.chimu.domain.model.Project
import software.setixx.chimu.domain.model.ProjectStatus
import software.setixx.chimu.domain.model.Team
import software.setixx.chimu.presentation.components.EmptyStateCard
import software.setixx.chimu.presentation.components.GameJamCard
import software.setixx.chimu.presentation.components.GameJamsContent
import software.setixx.chimu.presentation.components.HomeContent
import software.setixx.chimu.presentation.components.JudgingContent
import software.setixx.chimu.presentation.components.ProjectCard
import software.setixx.chimu.presentation.components.ProjectsContent
import software.setixx.chimu.presentation.components.TeamCard
import software.setixx.chimu.presentation.components.TeamsContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLogout: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToCreateTeam: () -> Unit,
    onNavigateToTeam: (String) -> Unit,
    onNavigateToJoinTeam: () -> Unit,
    onNavigateToCreateJam: () -> Unit,
    onNavigateToJamDetails: (String) -> Unit,
    viewModel: MainViewModel = koinViewModel()
) {
    var selectedDestination by remember { mutableStateOf(NavigationDestination.HOME) }
    var isRailExpanded by remember { mutableStateOf(true) }
    var showUserMenu by remember { mutableStateOf(false) }
    var showNotifications by remember { mutableStateOf(false) }

    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val availableDestinations = remember(state.user?.role) {
        when (state.user?.role) {
            "ADMIN" -> NavigationDestination.entries.toList()
            "ORGANIZER" -> listOf(
                NavigationDestination.HOME,
                NavigationDestination.GAME_JAMS,
                NavigationDestination.JUDGING
            )
            "JUDGE" -> listOf(
                NavigationDestination.HOME,
                NavigationDestination.GAME_JAMS,
                NavigationDestination.JUDGING
            )
            else -> listOf(
                NavigationDestination.HOME,
                NavigationDestination.GAME_JAMS,
                NavigationDestination.TEAMS,
                NavigationDestination.PROJECTS
            )
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(selectedDestination.title) },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, "Обновить")
                    }

                    Box {
                        BadgedBox(
                            badge = {
                                if (state.notificationCount > 0) {
                                    Badge {
                                        Text("${state.notificationCount}")
                                    }
                                }
                            }
                        ) {
                            IconButton(onClick = { showNotifications = true }) {
                                Icon(Icons.Default.Notifications, "Уведомления")
                            }
                        }

                        DropdownMenu(
                            expanded = showNotifications,
                            onDismissRequest = { showNotifications = false }
                        ) {
                            Text(
                                text = "Уведомления",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(16.dp)
                            )

                            if (state.notifications.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Нет новых уведомлений") },
                                    onClick = { },
                                    enabled = false
                                )
                            } else {
                                state.notifications.forEach { notification ->
                                    DropdownMenuItem(
                                        text = { Text(notification.message) },
                                        onClick = { showNotifications = false },
                                        leadingIcon = {
                                            Icon(notification.icon, null)
                                        }
                                    )
                                    if (notification != state.notifications.last()) {
                                        HorizontalDivider()
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box {
                        IconButton(
                            onClick = { showUserMenu = true },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Person,
                                        "Профиль",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }

                        DropdownMenu(
                            expanded = showUserMenu,
                            onDismissRequest = { showUserMenu = false }
                        ) {
                            state.user?.let { user ->
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = user.nickname,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = user.email,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                HorizontalDivider()
                            }

                            DropdownMenuItem(
                                text = { Text("Профиль") },
                                onClick = {
                                    showUserMenu = false
                                    onNavigateToProfile()
                                },
                                leadingIcon = { Icon(Icons.Default.Person, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Настройки") },
                                onClick = { showUserMenu = false },
                                leadingIcon = { Icon(Icons.Default.Settings, null) }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Выйти") },
                                onClick = {
                                    showUserMenu = false
                                    viewModel.onLogout(onLogout)
                                },
                                leadingIcon = { Icon(Icons.Default.ExitToApp, null) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            NavigationRail(
                modifier = Modifier
                    .fillMaxHeight()
                    .then(if (isRailExpanded) Modifier.width(250.dp) else Modifier),
                header = {
                    IconButton(onClick = { isRailExpanded = !isRailExpanded }) {
                        Icon(
                            if (isRailExpanded) Icons.Default.MenuOpen else Icons.Default.Menu,
                            if (isRailExpanded) "Свернуть меню" else "Развернуть меню"
                        )
                    }
                }
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                availableDestinations.forEach { destination ->
                    if (isRailExpanded) {
                        NavigationDrawerItem(
                            selected = selectedDestination == destination,
                            onClick = { selectedDestination = destination },
                            icon = { Icon(destination.icon, destination.title) },
                            label = { Text(destination.title) },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    } else {
                        NavigationRailItem(
                            selected = selectedDestination == destination,
                            onClick = { selectedDestination = destination },
                            icon = { Icon(destination.icon, destination.title) },
                            label = { Text(destination.title) },
                            alwaysShowLabel = true
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                when (selectedDestination) {
                    NavigationDestination.HOME -> HomeContent(state, onNavigateToTeam, onNavigateToJamDetails)
                    NavigationDestination.GAME_JAMS -> GameJamsContent(state, onNavigateToCreateJam, onNavigateToJamDetails)
                    NavigationDestination.TEAMS -> TeamsContent(state, onNavigateToCreateTeam, onNavigateToJoinTeam, onNavigateToTeam)
                    NavigationDestination.PROJECTS -> ProjectsContent(state)
                    NavigationDestination.JUDGING -> JudgingContent(state)
                }
            }
        }
    }
}

enum class NavigationDestination(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    HOME("Главная", Icons.Default.Home),
    GAME_JAMS("Game Jams", Icons.Default.Event),
    TEAMS("Мои команды", Icons.Default.Group),
    PROJECTS("Проекты", Icons.Default.Gamepad),
    JUDGING("Оценивание", Icons.Default.Star)
}