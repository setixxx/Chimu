package software.setixx.chimu.presentation.jam.details

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.koin.compose.viewmodel.koinViewModel
import software.setixx.chimu.api.domain.GameJamStatus
import software.setixx.chimu.presentation.jam.details.judging.JudgingScreen
import software.setixx.chimu.presentation.jam.details.leaderboard.LeaderboardScreen
import software.setixx.chimu.presentation.jam.details.management.ManagementScreen
import software.setixx.chimu.presentation.jam.details.progress.ProgressScreen
import software.setixx.chimu.presentation.jam.details.registration.RegistrationScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JamDetailsScreen(
    jamId: String,
    onBack: () -> Unit,
    onEditJam: (String) -> Unit,
    section: JamDetailsSection? = null,
    viewModel: JamDetailsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }

    LaunchedEffect(jamId) {
        viewModel.loadJamDetails(jamId)
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.jamDetails?.name ?: "Загрузка...") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                },
                actions = {
                    if (state.canEdit){
                        IconButton(onClick = { onEditJam(jamId) }) {
                            Icon(Icons.Default.Edit, "Редактировать")
                        }
                    }
                    if (state.canDelete) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                "Удалить",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    if (state.canCancel){
                        IconButton(onClick = { showCancelDialog = true }) {
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    val jamStatus = jam.status
                    when (state.resolveSection(section, jamStatus)) {
                        JamDetailsSection.Judging -> {
                            JudgingScreen(
                                jamId = jamId,
                                jam = jam,
                                userRole = state.userRole
                            )
                        }
                        JamDetailsSection.Registration -> {
                            RegistrationScreen(
                                jamId = jamId,
                                jam = jam,
                                userRole = state.userRole
                            )
                        }
                        JamDetailsSection.Progress -> {
                            ProgressScreen(
                                jamId = jamId,
                                jam = jam,
                                userRole = state.userRole,
                                userId = state.userId
                            )
                        }
                        JamDetailsSection.Management -> {
                            ManagementScreen(
                                jamId = jamId,
                                jam = jam
                            )
                        }
                        JamDetailsSection.AccessDenied -> {
                            AccessDeniedContent()
                        }
                        JamDetailsSection.Leaderboard -> {
                            LeaderboardScreen(
                                jamId = jamId,
                                jam = jam,
                                isAdminOrOrganizer = state.isAdminOrOrganizer
                            )
                        }
                    }
                }
            }
        }
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

enum class JamDetailsSection {
    Registration,
    Progress,
    Judging,
    Management,
    Leaderboard,
    AccessDenied
}

    private fun JamDetailsState.resolveSection(
        requestedSection: JamDetailsSection?,
        jamStatus: GameJamStatus
    ): JamDetailsSection {
        if (requestedSection == JamDetailsSection.Leaderboard) {
            return JamDetailsSection.Leaderboard
        }
        if (isAdminOrOrganizer) {
            return if (requestedSection == null && jamStatus == GameJamStatus.COMPLETED)
                JamDetailsSection.Leaderboard
            else
                JamDetailsSection.Management
        }
        return when (requestedSection) {
            JamDetailsSection.Management -> JamDetailsSection.AccessDenied
            JamDetailsSection.Registration -> if (isParticipant) JamDetailsSection.Registration else JamDetailsSection.AccessDenied
            JamDetailsSection.Progress    -> if (isParticipant || isJudge) JamDetailsSection.Progress else JamDetailsSection.AccessDenied
            JamDetailsSection.Judging     -> if (isParticipant || isJudge) JamDetailsSection.Judging else JamDetailsSection.AccessDenied
            JamDetailsSection.AccessDenied -> JamDetailsSection.AccessDenied
            null -> defaultSection(jamStatus)
            else -> JamDetailsSection.AccessDenied
        }
    }

    private fun JamDetailsState.defaultSection(jamStatus: GameJamStatus): JamDetailsSection {
        return when {
            jamStatus == GameJamStatus.COMPLETED -> JamDetailsSection.Leaderboard
            isParticipant && jamStatus == GameJamStatus.IN_PROGRESS -> JamDetailsSection.Progress
            isParticipant && jamStatus == GameJamStatus.JUDGING -> JamDetailsSection.Judging
            isParticipant -> JamDetailsSection.Registration
            isJudge && (jamStatus == GameJamStatus.JUDGING || jamStatus == GameJamStatus.COMPLETED) -> JamDetailsSection.Judging
            isJudge -> JamDetailsSection.Progress
            else -> JamDetailsSection.AccessDenied
        }
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
