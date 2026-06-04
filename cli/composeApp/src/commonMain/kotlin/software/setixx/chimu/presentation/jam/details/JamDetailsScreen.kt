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
import org.koin.compose.viewmodel.koinViewModel
import software.setixx.chimu.api.domain.TransferStatus
import software.setixx.chimu.presentation.jam.details.components.HomeBottomBar
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                },
                actions = {
                    if (state.canEdit){
                        FilledTonalIconButton(onClick = { onEditJam(jamId) }) {
                            Icon(Icons.Default.Edit, "Редактировать")
                        }
                    }
                    if (state.isAdminOrOrganizer || state.isPreviousOrganizer) {
                        FilledTonalIconButton(
                            onClick = { viewModel.openTransferDialog() },
                            enabled = state.canTransferEnabled
                        ) {
                            Icon(transferIcon, contentDescription = "Передача джема")
                        }
                    }
                    if (state.canDelete) {
                        FilledTonalIconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                "Удалить",
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
                            )
                        ) {
                            Icon(
                                Icons.Default.Cancel,
                                "Отменить",
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
        snackbarHost = { SnackbarHost(snackBarHostState) }
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
                                        }
                                    )
                                }
                                JamDetailsTab.Project -> {
                                    ProjectScreen(
                                        jamId = jamId,
                                        jam = jam,
                                        userRole = state.userRole,
                                        userId = state.userId,
                                        onNavigateToProject = onNavigateToProject,
                                        paddingValues = paddingValues
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
                                        }
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
                                        paddingValues = paddingValues
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
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
            title = { Text("Удалить джем?") },
            text = { Text("Все данные, включая регистрации и проекты, будут безвозвратно удалены.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteJam(jamId)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Отмена") }
            }
        )
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Отменить джем?") },
            text = { Text("Джем будет безвозратно отменен.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.cancelJam(jamId)
                        showCancelDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Отменить") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Отмена") }
            }
        )
    }
}

/**
 * Вкладки экрана деталей Game Jam.
 * Содержит информацию для отображения иконок и названий разделов.
 */
enum class JamDetailsTab(val label: String, val filledIcon: ImageVector, val outlinedIcon: ImageVector) {
    Overview("Overview", Icons.Filled.Home, Icons.Outlined.Home),
    Project("Project", Icons.Filled.Gamepad, Icons.Outlined.Gamepad),
    Judging("Judging", Icons.Filled.Gavel, Icons.Outlined.Gavel),
    Leaderboard("Leaderboard", Icons.Filled.Leaderboard, Icons.Outlined.Leaderboard),
    Management("Management", Icons.Filled.AdminPanelSettings, Icons.Outlined.AdminPanelSettings),
    AccessDenied("AccessDenied", Icons.Filled.Block, Icons.Filled.Block)
}

@Composable
private fun AccessDeniedContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "У вас нет доступа к этому разделу джема",
            style = MaterialTheme.typography.titleMedium
        )
    }
}
