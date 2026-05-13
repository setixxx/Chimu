package software.setixx.chimu.presentation.main

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Gamepad
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import software.setixx.chimu.api.domain.UserRole
import software.setixx.chimu.presentation.main.components.GameJamsContent
import software.setixx.chimu.presentation.main.components.HomeContent
import software.setixx.chimu.presentation.main.components.JudgingContent
import software.setixx.chimu.presentation.main.components.ProjectsContent
import software.setixx.chimu.presentation.main.components.TeamsContent

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
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
    val groupInteractionSource = remember { MutableInteractionSource() }
    var showUserMenu by remember { mutableStateOf(false) }

    var showNotifications by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    val railState = rememberWideNavigationRailState(initialValue = WideNavigationRailValue.Collapsed)
    val scope = rememberCoroutineScope()

    val state by viewModel.state.collectAsState()
    var selectedDestination by remember { mutableStateOf(NavigationDestination.HOME) }
    val availableDestinations = remember(state.user?.role) {
        when (state.user?.role) {
            UserRole.ADMIN -> NavigationDestination.entries.toList()
            UserRole.ORGANIZER -> listOf(
                NavigationDestination.HOME,
                NavigationDestination.GAME_JAMS,
                NavigationDestination.JUDGING
            )
            UserRole.JUDGE -> listOf(
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

                        DropdownMenuPopup(
                            modifier = Modifier
                                .widthIn(200.dp, 400.dp),
                            expanded = showUserMenu,
                            onDismissRequest = { showUserMenu = false }
                        ) {
                            val groupCount = 3

                            DropdownMenuGroup(
                                shapes = MenuDefaults.groupShape(index = 0, count = groupCount),
                                interactionSource = groupInteractionSource,
                                ) {
                                state.user?.let { user ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(MenuDefaults.DropdownMenuItemContentPadding)
                                            .padding(vertical = 8.dp),
                                        horizontalAlignment = Alignment.Start
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
                                }
                            }
                            Spacer(Modifier.height(MenuDefaults.GroupSpacing))

                            DropdownMenuGroup(
                                modifier = Modifier
                                    .padding(vertical = 4.dp),
                                shapes = MenuDefaults.groupShape(index = 1, count = groupCount),
                                interactionSource = groupInteractionSource,
                            ) {
                                DropdownMenuItem(
                                    onClick = {
                                        showUserMenu = false
                                        onNavigateToProfile()
                                    },
                                    text = { Text("Профиль") },
                                    leadingIcon = {
                                        Icon(Icons.Outlined.Person, contentDescription = null)
                                    },
                                    shape = MaterialTheme.shapes.medium
                                )
                                DropdownMenuItem(
                                    text = { Text("Настройки") },
                                    onClick = { showUserMenu = false },
                                    leadingIcon = {
                                        Icon(Icons.Outlined.Settings, contentDescription = null)
                                    },
                                    shape = MaterialTheme.shapes.medium
                                )
                            }
                            Spacer(Modifier.height(MenuDefaults.GroupSpacing))

                            DropdownMenuGroup(
                                modifier = Modifier
                                    .padding(vertical = 4.dp),
                                shapes = MenuDefaults.groupShape(index = 2, count = groupCount),
                                interactionSource = groupInteractionSource,
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text("Выйти", color = MaterialTheme.colorScheme.error)
                                    },
                                    onClick = {
                                        showUserMenu = false
                                        viewModel.onLogout(onLogout)
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.AutoMirrored.Outlined.ExitToApp,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    },
                                    shape = MaterialTheme.shapes.medium
                                )
                            }
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
                .fillMaxWidth()
                .padding(paddingValues),
            horizontalArrangement = Arrangement.Center
        ) {
            val headerDescription = if (railState.targetValue == WideNavigationRailValue.Expanded) {
                "Свернуть меню"
            } else {
                "Развернуть меню"
            }

            WideNavigationRail(
                state = railState,
                header = {
                    TooltipBox(
                        positionProvider =
                            TooltipDefaults.rememberTooltipPositionProvider(
                                TooltipAnchorPosition.Above
                            ),
                        tooltip = { PlainTooltip { Text(headerDescription) } },
                        state = rememberTooltipState(),
                    ){
                        IconButton(
                            modifier =
                                Modifier.padding(start = 24.dp).semantics {
                                    stateDescription =
                                        if (railState.currentValue == WideNavigationRailValue.Expanded) {
                                            "Expanded"
                                        } else {
                                            "Collapsed"
                                        }
                                },
                            onClick = {
                                scope.launch {
                                    if (railState.currentValue == WideNavigationRailValue.Expanded) {
                                        railState.collapse()
                                    } else {
                                        railState.expand()
                                    }
                                }
                            }
                        ) {
                            if (railState.targetValue == WideNavigationRailValue.Expanded) {
                                Icon(Icons.AutoMirrored.Filled.MenuOpen, headerDescription)
                            } else {
                                Icon(Icons.Filled.Menu, headerDescription)
                            }
                        }
                    }
                }
            ) {
                availableDestinations.forEach { destination ->
                    val selected = selectedDestination == destination
                    WideNavigationRailItem(
                        selected = selected,
                        onClick = { selectedDestination = destination},
                        railExpanded = railState.currentValue == WideNavigationRailValue.Expanded,
                        icon = {
                            if (selected){
                                Icon(
                                    destination.selectedIcon,
                                    destination.title
                                )
                            } else {
                                Icon(destination.unselectedIcon,
                                    destination.title)
                            }
                        },
                        label = {
                            Text(destination.title, textAlign = TextAlign.Center)
                        }
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(MaterialTheme.shapes.extraLarge),
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
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("Главная", Icons.Filled.Home, Icons.Outlined.Home),
    GAME_JAMS("Джемы", Icons.Filled.Event, Icons.Outlined.Event),
    TEAMS("Мои команды", Icons.Filled.Group, Icons.Outlined.Group),
    PROJECTS("Проекты", Icons.Filled.Gamepad, Icons.Outlined.Gamepad),
    JUDGING("Оценивание", Icons.Filled.Star, Icons.Outlined.Star)
}