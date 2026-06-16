package software.setixx.chimu.presentation.main.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.WideNavigationRail
import androidx.compose.material3.WideNavigationRailItem
import androidx.compose.material3.WideNavigationRailState
import androidx.compose.material3.WideNavigationRailValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import chimu.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import software.setixx.chimu.presentation.main.MainState
import software.setixx.chimu.presentation.main.NavigationDestination
import software.setixx.chimu.presentation.main.Notification
import software.setixx.chimu.presentation.main.home.HomeContent
import software.setixx.chimu.presentation.main.jam.GameJamsContent
import software.setixx.chimu.presentation.main.judge.JudgingContent
import software.setixx.chimu.presentation.main.project.ProjectsContent
import software.setixx.chimu.presentation.main.team.TeamsContent

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MobileModalLayout(
    railState: WideNavigationRailState,
    scope: CoroutineScope,
    availableDestinations: List<NavigationDestination>,
    selectedDestination: NavigationDestination,
    onDestinationSelected: (NavigationDestination) -> Unit,
    state: MainState,
    onNavigateToCreateTeam: () -> Unit,
    onNavigateToTeam: (String) -> Unit,
    onNavigateToJoinTeam: () -> Unit,
    onNavigateToCreateJam: () -> Unit,
    onNavigateToJamDetails: (String) -> Unit,
    onNavigateToJamJudging: (String) -> Unit,
    showNotifications: Boolean,
    onShowNotifications: (Boolean) -> Unit,
    showUserMenu: Boolean,
    onShowUserMenu: (Boolean) -> Unit,
    groupInteractionSource: MutableInteractionSource,
    onNavigateToProfile: () -> Unit,
    onChangePasswordClick: () -> Unit,
    snackBarHostState: SnackbarHostState,
    onRefresh: () -> Unit,
    onLogoutClick: () -> Unit,
    onNotificationAction: (Notification) -> Unit,
    onNavigateToProject: (String, String?, Boolean) -> Unit
) {
    val isExpanded by remember {
        derivedStateOf { railState.targetValue == WideNavigationRailValue.Expanded }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(
                            modifier = Modifier.padding(start = 24.dp).alpha(0f),
                            onClick = {}
                        ) {
                            Icon(Icons.Filled.Menu, contentDescription = null)
                        }
                    },
                    title = {
                        Text(stringResource(selectedDestination.titleRes))
                    },
                    actions = {
                        IconButton(onClick = onRefresh) {
                            Icon(Icons.Default.Refresh, stringResource(Res.string.main_refresh_desc))
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
                                IconButton(onClick = { onShowNotifications(true) }) {
                                    Icon(Icons.Default.Notifications, stringResource(Res.string.main_notifications_desc))
                                }
                            }

                            DropdownMenuPopup(
                                expanded = showNotifications,
                                onDismissRequest = { onShowNotifications(false) }
                            ) {
                                DropdownMenuGroup(
                                    shapes = MenuDefaults.groupShapes(MaterialTheme.shapes.medium),
                                    interactionSource = groupInteractionSource,
                                ) {
                                    Text(
                                        text = stringResource(Res.string.main_notifications_title),
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(16.dp)
                                    )

                                    if (state.notifications.isEmpty()) {
                                        DropdownMenuItem(
                                            text = { Text(stringResource(Res.string.main_notifications_empty)) },
                                            onClick = { },
                                            enabled = false
                                        )
                                    } else {
                                        state.notifications.forEach { notification ->
                                            DropdownMenuItem(
                                                text = { Text(notification.message) },
                                                onClick = {
                                                    onShowNotifications(false)
                                                    onNotificationAction(notification)
                                                },
                                                leadingIcon = {
                                                    Icon(
                                                        notification.icon,
                                                        contentDescription = null,
                                                        tint = if (notification.actionType != null)
                                                            MaterialTheme.colorScheme.primary
                                                        else
                                                            LocalContentColor.current
                                                    )
                                                },
                                                trailingIcon = if (notification.actionType != null) {
                                                    { Icon(Icons.Default.ChevronRight, contentDescription = null) }
                                                } else null,
                                                shape = MaterialTheme.shapes.medium
                                            )

                                            if (notification != state.notifications.last()) {
                                                HorizontalDivider()
                                            }
                                            Spacer(Modifier.height(MenuDefaults.GroupSpacing))
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Box {
                            IconButton(
                                onClick = { onShowUserMenu(true) },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Person,
                                            stringResource(Res.string.main_profile_desc),
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }

                            DropdownMenuPopup(
                                modifier = Modifier
                                    .widthIn(200.dp, 400.dp),
                                expanded = showUserMenu,
                                onDismissRequest = { onShowUserMenu(false) }
                            ) {
                                DropdownMenuGroup(
                                    shapes = MenuDefaults.groupShapes(MaterialTheme.shapes.medium),
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
                                    DropdownMenuItem(
                                        onClick = {
                                            onShowUserMenu(false)
                                            onNavigateToProfile()
                                        },
                                        text = { Text(stringResource(Res.string.main_profile_button)) },
                                        leadingIcon = {
                                            Icon(Icons.Outlined.Person, contentDescription = null)
                                        },
                                        shape = MaterialTheme.shapes.medium
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(Res.string.main_change_password_button)) },
                                        onClick = {
                                            onShowUserMenu(false)
                                            onChangePasswordClick()
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Outlined.LockReset, contentDescription = null)
                                        },
                                        shape = MaterialTheme.shapes.medium
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Text(stringResource(Res.string.main_logout_button), color = MaterialTheme.colorScheme.error)
                                        },
                                        onClick = {
                                            onShowUserMenu(false)
                                            onLogoutClick()
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
                                    Spacer(Modifier.height(MenuDefaults.GroupSpacing))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackBarHostState) }
        ){ paddingValues ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .clip(MaterialTheme.shapes.largeIncreased),
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                when (selectedDestination) {
                    NavigationDestination.HOME -> HomeContent(
                        state,
                        onNavigateToTeam,
                        onNavigateToJamDetails,
                        onNavigateToProject
                    )
                    NavigationDestination.GAME_JAMS -> GameJamsContent(
                        state,
                        onNavigateToCreateJam,
                        onNavigateToJamDetails
                    )
                    NavigationDestination.TEAMS -> TeamsContent(
                        state,
                        onNavigateToCreateTeam,
                        onNavigateToJoinTeam,
                        onNavigateToTeam
                    )
                    NavigationDestination.PROJECTS -> ProjectsContent(state, onNavigateToProject)
                    NavigationDestination.JUDGING -> JudgingContent(state, onNavigateToJamJudging)
                }
            }
        }
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(tween(250)),
            exit = fadeOut(tween(250))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.32f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        scope.launch { railState.collapse() }
                    }
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = slideInHorizontally(tween(300)) { -it },
            exit = slideOutHorizontally(tween(300)) { -it }
        ) {
            WideNavigationRail(
                modifier = Modifier.fillMaxHeight(),
                state = railState,
                shape = MaterialTheme.shapes.largeIncreased
            ) {
                Spacer(modifier = Modifier.height(64.dp))

                availableDestinations.forEach { destination ->
                    val selected = selectedDestination == destination
                    WideNavigationRailItem(
                        selected = selected,
                        onClick = {
                            onDestinationSelected(destination)
                            scope.launch { railState.collapse() }
                        },
                        railExpanded = true,
                        icon = {
                            if (selected) {
                                Icon(destination.selectedIcon, stringResource(destination.titleRes))
                            } else {
                                Icon(destination.unselectedIcon, stringResource(destination.titleRes))
                            }
                        },
                        label = { Text(stringResource(destination.titleRes), textAlign = TextAlign.Center) }
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .windowInsetsPadding(TopAppBarDefaults.windowInsets)
                .height(64.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            val headerDescription = if (isExpanded) stringResource(Res.string.main_menu_collapse_desc) else stringResource(Res.string.main_menu_expand_desc)
            val expandedStr = stringResource(Res.string.main_nav_expanded)
            val collapsedStr = stringResource(Res.string.main_nav_collapsed)

            IconButton(
                modifier = Modifier
                    .padding(start = 24.dp)
                    .semantics {
                        stateDescription = if (isExpanded) expandedStr else collapsedStr
                    },
                onClick = {
                    scope.launch {
                        if (isExpanded) railState.collapse() else railState.expand()
                    }
                }
            ) {
                Crossfade(
                    targetState = isExpanded,
                    animationSpec = tween(durationMillis = 250),
                    label = "MenuIconAnimation"
                ) { expanded ->
                    if (expanded) {
                        Icon(
                            Icons.AutoMirrored.Filled.MenuOpen,
                            headerDescription,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    } else {
                        Icon(
                            Icons.Filled.Menu,
                            headerDescription,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
