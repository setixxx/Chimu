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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLogout: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToCreateTeam: () -> Unit,
    onNavigateToTeam: (String) -> Unit,
    onNavigateToJoinTeam: () -> Unit,
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
                    NavigationDestination.HOME -> HomeContent(state, onNavigateToTeam)
                    NavigationDestination.GAME_JAMS -> GameJamsContent(state)
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

@Composable
fun HomeContent(state: MainState, onNavigateToTeam: (String) -> Unit) {
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
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text(
                text = "Активные Game Jams",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        if (state.activeJams.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.Event,
                    title = "Нет активных джемов",
                    description = "Джемы появятся здесь, когда начнется регистрация"
                )
            }
        } else {
            items(state.activeJams.size) { index ->
                GameJamCard(jam = state.activeJams[index])
            }
        }

        if (state.userTeams.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Мои команды",
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.userTeams.size) { index ->
                        TeamCard(team = state.userTeams[index], onClick = { onNavigateToTeam(state.userTeams[index].id) })
                    }
                }
            }
        }

        if (state.userProjects.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Мои проекты",
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            items(state.userProjects.size) { index ->
                ProjectCard(project = state.userProjects[index])
            }
        }
    }
}

@Composable
fun GameJamCard(jam: GameJam) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = jam.name,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                StatusChip(status = jam.status)
            }

            jam.theme?.let { theme ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Тема: $theme",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Group,
                        null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${jam.registeredTeamsCount} команд",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                jam.daysRemaining?.let { days ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Schedule,
                            null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$days ${getDaysWord(days)} осталось",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
fun StatusChip(status: GameJamStatus) {
    val (text, color) = when (status) {
        GameJamStatus.REGISTRATION_OPEN -> "Регистрация" to MaterialTheme.colorScheme.tertiary
        GameJamStatus.IN_PROGRESS -> "В процессе" to MaterialTheme.colorScheme.primary
        GameJamStatus.JUDGING -> "Оценивание" to MaterialTheme.colorScheme.secondary
        GameJamStatus.COMPLETED -> "Завершен" to MaterialTheme.colorScheme.surfaceVariant
        GameJamStatus.CANCELLED -> "Отменен" to MaterialTheme.colorScheme.error
        else -> status.name to MaterialTheme.colorScheme.surfaceVariant
    }

    AssistChip(
        onClick = { },
        label = { Text(text) },
        leadingIcon = {
            Icon(
                Icons.Default.Circle,
                null,
                modifier = Modifier.size(8.dp),
                tint = color
            )
        }
    )
}

@Composable
fun TeamCard(team: Team, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = team.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (team.isLeader) {
                    Icon(
                        Icons.Default.Star,
                        "Лидер",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            team.description?.let { desc ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Person,
                    null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${team.memberCount} участников",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ProjectCard(project: Project) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = project.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                ProjectStatusChip(status = project.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Джем: ${project.jamName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            project.teamName?.let { teamName ->
                Text(
                    text = "Команда: $teamName",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ProjectStatusChip(status: ProjectStatus) {
    val (text, color) = when (status) {
        ProjectStatus.DRAFT -> "Черновик" to MaterialTheme.colorScheme.surfaceVariant
        ProjectStatus.SUBMITTED -> "Отправлен" to MaterialTheme.colorScheme.primary
        ProjectStatus.PUBLISHED -> "Опубликован" to MaterialTheme.colorScheme.tertiary
        ProjectStatus.DISQUALIFIED -> "Дисквалифицирован" to MaterialTheme.colorScheme.error
        else -> status.name to MaterialTheme.colorScheme.surfaceVariant
    }

    AssistChip(
        onClick = { },
        label = { Text(text, style = MaterialTheme.typography.labelSmall) },
        leadingIcon = {
            Icon(
                Icons.Default.Circle,
                null,
                modifier = Modifier.size(6.dp),
                tint = color
            )
        }
    )
}

@Composable
fun EmptyStateCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String? = null
) {
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
                icon,
                null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun GameJamsContent(state: MainState) {
    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Все Game Jams",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        if (state.activeJams.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.Event,
                    title = "Нет джемов"
                )
            }
        } else {
            items(state.activeJams.size) { index ->
                GameJamCard(jam = state.activeJams[index])
            }
        }
    }
}

@Composable
fun TeamsContent(
    state: MainState,
    onNavigateToCreateTeam: () -> Unit,
    onNavigateToJoinTeam: () -> Unit,
    onNavigateToTeam: (String) -> Unit
) {
    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Мои команды",
                    style = MaterialTheme.typography.headlineMedium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onNavigateToJoinTeam) {
                        Icon(Icons.Default.Login, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Присоединиться")
                    }
                    Button(onClick = onNavigateToCreateTeam) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Создать")
                    }
                }
            }
        }

        if (state.userTeams.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.Group,
                    title = "У вас пока нет команд",
                    description = "Создайте команду или присоединитесь к существующей"
                )
            }
        } else {
            items(state.userTeams.size) { index ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToTeam(state.userTeams[index].id) },
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = state.userTeams[index].name,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            if (state.userTeams[index].isLeader) {
                                Icon(
                                    Icons.Default.Star,
                                    "Лидер",
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        state.userTeams[index].description?.let { desc ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Person,
                                null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${state.userTeams[index].memberCount} участников",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun JudgingContent(state: MainState) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Оценивание",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Функционал оценивания в разработке",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ProjectsContent(state: MainState) {
    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Мои проекты",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        if (state.userProjects.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.Gamepad,
                    title = "У вас пока нет проектов",
                    description = "Зарегистрируйтесь на джем и создайте проект"
                )
            }
        } else {
            items(state.userProjects.size) { index ->
                ProjectCard(project = state.userProjects[index])
            }
        }
    }
}

private fun getDaysWord(days: Int): String {
    return when {
        days % 10 == 1 && days % 100 != 11 -> "день"
        days % 10 in 2..4 && days % 100 !in 12..14 -> "дня"
        else -> "дней"
    }
}