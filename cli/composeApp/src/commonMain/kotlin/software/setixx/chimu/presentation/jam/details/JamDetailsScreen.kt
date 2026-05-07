package software.setixx.chimu.presentation.jam.details

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.koin.compose.viewmodel.koinViewModel
import software.setixx.chimu.api.domain.GameJamStatus
import software.setixx.chimu.presentation.jam.details.judging.JudgingScreen
import software.setixx.chimu.presentation.jam.details.management.ManagementScreen
import software.setixx.chimu.presentation.jam.details.progress.ProgressScreen
import software.setixx.chimu.presentation.jam.details.registration.RegistrationScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JamDetailsScreen(
    jamId: String,
    onBack: () -> Unit,
    onEditJam: (String) -> Unit,
    onOpenManagement: (String) -> Unit = {},
    section: JamDetailsSection? = null,
    viewModel: JamDetailsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }

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
                        Icon(Icons.Default.ArrowBack, "Назад")
                    }
                },
                actions = {
                    if (state.canEdit) {
                        IconButton(onClick = { onEditJam(jamId) }) {
                            Icon(Icons.Default.Edit, "Редактировать")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                "Удалить",
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
                    val jamStatus = try {
                        GameJamStatus.valueOf(jam.status)
                    } catch (e: Exception) {
                        GameJamStatus.DRAFT
                    }

                    when (section ?: state.defaultSection(jamStatus)) {
                        JamDetailsSection.Judging -> {
                            JudgingScreen(jamId = jamId, jam = jam)
                        }
                        JamDetailsSection.Registration -> {
                            RegistrationScreen(
                                jamId = jamId,
                                jam = jam,
                                userRole = state.userRole,
                                userId = state.userId
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
                            if (jamStatus == GameJamStatus.IN_PROGRESS) {
                                ProgressScreen(
                                    jamId = jamId,
                                    jam = jam,
                                    userRole = state.userRole,
                                    userId = state.userId
                                )
                            } else {
                                ManagementScreen(
                                    jamId = jamId,
                                    jam = jam
                                )
                            }
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
}

enum class JamDetailsSection {
    Registration,
    Progress,
    Judging,
    Management
}

private fun JamDetailsState.defaultSection(jamStatus: GameJamStatus): JamDetailsSection {
    return when {
        jamStatus == GameJamStatus.JUDGING || jamStatus == GameJamStatus.COMPLETED -> JamDetailsSection.Judging
        jamStatus == GameJamStatus.IN_PROGRESS -> JamDetailsSection.Progress
        else -> JamDetailsSection.Registration
    }
}
