package software.setixx.chimu.presentation.jam.details

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Gamepad
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import chimu.composeapp.generated.resources.*
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import software.setixx.chimu.api.domain.TransferStatus
import software.setixx.chimu.api.domain.UserRole
import software.setixx.chimu.presentation.jam.details.components.HomeBottomBar
import software.setixx.chimu.presentation.jam.details.forcestatus.ForceStatusDialog
import software.setixx.chimu.presentation.jam.details.judging.JudgingScreen
import software.setixx.chimu.presentation.jam.details.leaderboard.LeaderboardScreen
import software.setixx.chimu.presentation.jam.details.management.ManagementScreen
import software.setixx.chimu.presentation.jam.details.project.ProjectScreen
import software.setixx.chimu.presentation.jam.details.overview.OverviewScreen
import software.setixx.chimu.presentation.jam.details.transfer.JamTransferDialog

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun JamDetailsScreen(
    jamId: String,
    onBack: () -> Unit,
    onEditJam: (String) -> Unit,
    initialTab: JamDetailsTab? = null,
    onNavigateToProject: (String, String?, Boolean) -> Unit,
    onNavigateToProjectRating: (String, String) -> Unit,
    onNavigateToAlienProfile: (String) -> Unit,
    onNavigateToOwnProfile: () -> Unit,
    viewModel: JamDetailsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    val pagerState = rememberPagerState(
        pageCount = { state.availableTabs.size },
        initialPage = 0
    )

    val transferIcon = when {
        state.hasPendingTransfer -> Icons.Default.HourglassTop
        state.currentTransfer?.status == TransferStatus.ACCEPTED -> Icons.Default.DoneAll
        else -> Icons.Default.SwapHoriz
    }

    LaunchedEffect(jamId) {
        viewModel.loadJamDetails(jamId)
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackBarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) onBack()
    }

    LaunchedEffect(state.availableTabs, initialTab) {
        initialTab?.let { tab ->
            val targetPage = state.availableTabs.indexOf(tab)
            if (targetPage >= 0 && pagerState.currentPage != targetPage) {
                pagerState.scrollToPage(targetPage)
            }
        }
        if (pagerState.currentPage >= state.availableTabs.size && state.availableTabs.isNotEmpty()) {
            pagerState.scrollToPage(state.availableTabs.lastIndex)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(Color.Transparent),
                title = { },
                navigationIcon = {
                    FilledTonalIconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(Res.string.back))
                    }
                },
                actions = {
                    if (state.canEdit){
                        FilledTonalIconButton(
                            onClick = { onEditJam(jamId) },
                            enabled = state.canEdit
                        ) {
                            Icon(Icons.Default.Edit, stringResource(Res.string.edit))
                        }
                    }
                    if (state.canTransferJam) {
                        FilledTonalIconButton(
                            onClick = { viewModel.openTransferDialog() },
                            enabled = state.canTransferEnabled
                        ) {
                            Icon(transferIcon, contentDescription = stringResource(Res.string.jam_details_transfer_desc))
                        }
                    }
                    if (state.canForceJamStatus){
                        FilledTonalIconButton(
                            onClick = { viewModel.openForceStatusDialog() },
                        ) {
                            Icon(
                                Icons.Default.DoubleArrow,
                                stringResource(Res.string.jam_details_change_status_desc),
                            )
                        }
                    }
                    if (state.canDelete) {
                        FilledTonalIconButton(
                            onClick = { showDeleteDialog = true },
                            colors = IconButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.error,
                                disabledContainerColor = MaterialTheme.colorScheme.errorContainer,
                                disabledContentColor = MaterialTheme.colorScheme.errorContainer
                            ),
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                stringResource(Res.string.delete),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    if (state.canCancel){
                        FilledTonalIconButton(
                            onClick = { showCancelDialog = true },
                            colors = IconButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.error,
                                disabledContainerColor = MaterialTheme.colorScheme.errorContainer,
                                disabledContentColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            enabled = state.canCancel
                        ) {
                            Icon(
                                Icons.Default.Cancel,
                                stringResource(Res.string.cancel),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            HomeBottomBar(
                pagerState = pagerState,
                tabs = state.availableTabs
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackBarHostState
            )
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                LoadingIndicator()
            }
        } else {
            state.jamDetails?.let { jam ->
                HorizontalPager(
                    userScrollEnabled = true,
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                ) { page ->
                    val currentTab = state.availableTabs[page]

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .widthIn(max = 1500.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            when (currentTab) {
                                JamDetailsTab.Overview -> {
                                    OverviewScreen(
                                        jamId = jamId,
                                        jam = jam,
                                        userRole = state.userRole,
                                        userId = state.userId,
                                        onNavigateToOwnProfile = onNavigateToOwnProfile,
                                        onNavigateToAlienProfile = { userId ->
                                            onNavigateToAlienProfile(userId)
                                        },
                                        snackbarHostState = snackBarHostState
                                    )
                                }
                                JamDetailsTab.Project -> {
                                    ProjectScreen(
                                        jamId = jamId,
                                        jam = jam,
                                        userRole = state.userRole,
                                        userId = state.userId,
                                        onNavigateToProject = onNavigateToProject,
                                        paddingValues = paddingValues,
                                        snackbarHostState = snackBarHostState
                                    )
                                }
                                JamDetailsTab.Judging -> {
                                    JudgingScreen(
                                        jamId = jamId,
                                        jam = jam,
                                        onNavigateToProject = onNavigateToProject,
                                        onNavigateToProjectRating = { projectId ->
                                            onNavigateToProjectRating(jamId, projectId)
                                        },
                                        paddingValues = paddingValues
                                    )
                                }
                                JamDetailsTab.Management -> {
                                    ManagementScreen(
                                        jam = jam,
                                        paddingValues = paddingValues,
                                        onNavigateToAlienProfile = { judge ->
                                            onNavigateToAlienProfile(judge)
                                        },
                                        snackbarHostState = snackBarHostState
                                    )
                                }
                                JamDetailsTab.AccessDenied -> {
                                    AccessDeniedContent()
                                }
                                JamDetailsTab.Leaderboard -> {
                                    LeaderboardScreen(
                                        jamId = jamId,
                                        jam = jam,
                                        isAdminOrOrganizer = state.isAdminOrOrganizer,
                                        paddingValues = paddingValues,
                                        snackbarHostState = snackBarHostState
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (state.showForceStatusDialog){
        ForceStatusDialog(
            state = state,
            onDismiss = { viewModel.closeForceStatusDialog() },
            onSubmit = { viewModel.forceJamStatus(jamId, it) },
            onStatusSelected = { viewModel.onForceStatusSelected(it) }
        )
    }

    if (state.showTransferDialog) {
        JamTransferDialog(
            state = state,
            onDismiss = { viewModel.closeTransferDialog() },
            onQueryChange = { viewModel.onTransferRecipientQueryChange(it) },
            onSearch = { viewModel.searchRecipient() },
            onCreate = { viewModel.createTransfer(jamId) },
            onCancel = { viewModel.cancelTransfer(jamId) }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(Res.string.jam_details_delete_title)) },
            text = { Text(stringResource(Res.string.jam_details_delete_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteJam(jamId)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text(stringResource(Res.string.delete)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text(stringResource(Res.string.cancel)) }
            }
        )
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text(stringResource(Res.string.jam_details_cancel_title)) },
            text = { Text(stringResource(Res.string.jam_details_cancel_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.cancelJam(jamId)
                        showCancelDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text(stringResource(Res.string.cancel)) }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) { Text(stringResource(Res.string.cancel)) }
            }
        )
    }
}

/**
 * Вкладки экрана деталей Game Jam.
 * Содержит информацию для отображения иконок и названий разделов.
 */
enum class JamDetailsTab(val labelRes: StringResource, val filledIcon: ImageVector, val outlinedIcon: ImageVector) {
    Overview(Res.string.jam_details_tab_overview, Icons.Filled.Home, Icons.Outlined.Home),
    Project(Res.string.jam_details_tab_project, Icons.Filled.Gamepad, Icons.Outlined.Gamepad),
    Judging(Res.string.jam_details_tab_judging, Icons.Filled.Gavel, Icons.Outlined.Gavel),
    Leaderboard(Res.string.jam_details_tab_leaderboard, Icons.Filled.Leaderboard, Icons.Outlined.Leaderboard),
    Management(Res.string.jam_details_tab_management, Icons.Filled.AdminPanelSettings, Icons.Outlined.AdminPanelSettings),
    AccessDenied(Res.string.jam_details_tab_access_denied, Icons.Filled.Block, Icons.Filled.Block)
}

@Composable
private fun AccessDeniedContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(Res.string.jam_details_access_denied),
            style = MaterialTheme.typography.titleMedium
        )
    }
}
