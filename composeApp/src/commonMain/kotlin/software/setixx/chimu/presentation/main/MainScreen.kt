package software.setixx.chimu.presentation.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLogout: () -> Unit,
    viewModel: MainViewModel = koinViewModel()
) {
    var selectedDestination by remember { mutableStateOf(NavigationDestination.HOME) }
    var isRailExpanded by remember { mutableStateOf(true) }
    var showUserMenu by remember { mutableStateOf(false) }
    var showNotifications by remember { mutableStateOf(false) }

    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(selectedDestination.title) },
                actions = {
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
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Уведомления"
                                )
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
                                            Icon(notification.icon, contentDescription = null)
                                        }
                                    )
                                    if (notification != state.notifications.last()) {
                                        Divider()
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
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Профиль",
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
                                Divider()
                            }

                            DropdownMenuItem(
                                text = { Text("Профиль") },
                                onClick = {
                                    showUserMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Настройки") },
                                onClick = {
                                    showUserMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Settings, contentDescription = null)
                                }
                            )
                            Divider()
                            DropdownMenuItem(
                                text = { Text("Выйти") },
                                onClick = {
                                    showUserMenu = false
                                    viewModel.onLogout(onLogout)
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.ExitToApp, contentDescription = null)
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                }
            )
        }
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
                    IconButton(
                        onClick = { isRailExpanded = !isRailExpanded }
                    ) {
                        Icon(
                            imageVector = if (isRailExpanded)
                                Icons.Default.MenuOpen
                            else
                                Icons.Default.Menu,
                            contentDescription = if (isRailExpanded)
                                "Свернуть меню"
                            else
                                "Развернуть меню"
                        )
                    }
                }
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                NavigationDestination.entries.forEach { destination ->
                    if (isRailExpanded) {
                        NavigationDrawerItem(
                            selected = selectedDestination == destination,
                            onClick = { selectedDestination = destination },
                            icon = {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = destination.title
                                )
                            },
                            label = { Text(destination.title) },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    } else {
                        NavigationRailItem(
                            selected = selectedDestination == destination,
                            onClick = { selectedDestination = destination },
                            icon = {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = destination.title
                                )
                            },
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
                    NavigationDestination.HOME -> HomeContent(state)
                    NavigationDestination.GAME_JAMS -> GameJamsContent()
                    NavigationDestination.TEAMS -> TeamsContent()
                    NavigationDestination.PROJECTS -> ProjectsContent()
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
    PROJECTS("Проекты", Icons.Default.Gamepad)
}

@Composable
fun HomeContent(state: MainState) {
    if (state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Активные Game Jams",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        if (state.activeJams.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Нет активных джемов",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(state.activeJams.size) { index ->
                val jam = state.activeJams[index]
                GameJamCard(jam = jam)
            }
        }
    }
}

@Composable
fun GameJamCard(jam: GameJamPreview) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = jam.name,
                    style = MaterialTheme.typography.titleLarge
                )
                AssistChip(
                    onClick = { },
                    label = { Text(jam.status) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Circle,
                            contentDescription = null,
                            modifier = Modifier.size(8.dp),
                            tint = when (jam.status) {
                                "В процессе" -> MaterialTheme.colorScheme.primary
                                "Регистрация" -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.secondary
                            }
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            jam.theme?.let { theme ->
                Text(
                    text = "Тема: $theme",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${jam.teamsCount} команд",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                jam.daysRemaining?.let { days ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$days ${if (days == 1) "день" else if (days < 5) "дня" else "дней"} осталось",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Подробнее")
            }
        }
    }
}

@Composable
fun GameJamsContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Event,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Game Jams",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Здесь будет список всех джемов",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TeamsContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Group,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Мои команды",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Здесь будут ваши команды",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ProjectsContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Gamepad,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Проекты",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Здесь будут ваши проекты",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
