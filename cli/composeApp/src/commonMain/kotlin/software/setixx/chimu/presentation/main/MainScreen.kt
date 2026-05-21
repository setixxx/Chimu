package software.setixx.chimu.presentation.main

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Gamepad
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import software.setixx.chimu.api.domain.UserRole
import software.setixx.chimu.presentation.main.components.DesktopRailLayout
import software.setixx.chimu.presentation.main.components.MobileModalLayout

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

    val snackBarHostState = remember { SnackbarHostState() }
    val railState = rememberWideNavigationRailState(initialValue = WideNavigationRailValue.Collapsed)
    val scope = rememberCoroutineScope()

    val state by viewModel.state.collectAsState()
    var selectedDestination by rememberSaveable { mutableStateOf(NavigationDestination.HOME) }
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

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isCompactScreen = maxWidth < 600.dp

        if (isCompactScreen) {
            MobileModalLayout(
                railState = railState,
                scope = scope,
                availableDestinations = availableDestinations,
                selectedDestination = selectedDestination,
                onDestinationSelected = { selectedDestination = it },
                state = state,
                onNavigateToCreateTeam = onNavigateToCreateTeam,
                onNavigateToTeam = onNavigateToTeam,
                onNavigateToJoinTeam = onNavigateToJoinTeam,
                onNavigateToCreateJam = onNavigateToCreateJam,
                onNavigateToJamDetails = onNavigateToJamDetails,
                showNotifications = showNotifications,
                onShowNotifications = { showNotifications = it},
                showUserMenu = showUserMenu,
                onShowUserMenu = { showUserMenu = it},
                onNavigateToProfile = onNavigateToProfile,
                groupInteractionSource = groupInteractionSource,
                snackBarHostState = snackBarHostState,
                onRefresh = { viewModel.refresh() },
                onLogoutClick = { viewModel.onLogout(onLogout) },
            )
        } else {
            DesktopRailLayout(
                railState = railState,
                scope = scope,
                availableDestinations = availableDestinations,
                selectedDestination = selectedDestination,
                onDestinationSelected = { selectedDestination = it },
                state = state,
                onNavigateToCreateTeam = onNavigateToCreateTeam,
                onNavigateToTeam = onNavigateToTeam,
                onNavigateToJoinTeam = onNavigateToJoinTeam,
                onNavigateToCreateJam = onNavigateToCreateJam,
                onNavigateToJamDetails = onNavigateToJamDetails,
                showNotifications = showNotifications,
                onShowNotifications = { showNotifications = it},
                showUserMenu = showUserMenu,
                onShowUserMenu = { showUserMenu = it},
                onNavigateToProfile = onNavigateToProfile,
                groupInteractionSource = groupInteractionSource,
                snackBarHostState = snackBarHostState,
                onRefresh = { viewModel.refresh() },
                onLogoutClick = { viewModel.onLogout(onLogout) },
            )
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { error ->
            snackBarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
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