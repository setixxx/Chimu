package software.setixx.chimu.presentation.project

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import software.setixx.chimu.api.domain.ProjectStatus
import software.setixx.chimu.api.domain.UserRole
import software.setixx.chimu.presentation.jam.details.project.ProjectViewModel
import software.setixx.chimu.presentation.project.components.ProjectDetailsContent
import software.setixx.chimu.presentation.project.components.ProjectNotFoundContent

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProjectDetailsScreen(
    projectId: String,
    userRole: UserRole?,
    isAdminOrOrganizer: Boolean,
    onBack: () -> Unit,
    viewModel: ProjectViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(projectId) { viewModel.loadProjectById(projectId) }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    val project = state.userProject
    val isDisqualified = project?.status == ProjectStatus.DISQUALIFIED.name
    var showDisqualifyConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(project?.title ?: "Проект") },
                navigationIcon = {
                    FilledTonalIconButton(
                        onClick = onBack
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    if (isAdminOrOrganizer && !isDisqualified && project != null) {
                        FilledTonalIconButton(
                            onClick = { showDisqualifyConfirm = true },
                            colors = IconButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.error,
                                disabledContainerColor = MaterialTheme.colorScheme.errorContainer,
                                disabledContentColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Icon(
                                Icons.Default.Block,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        LoadingIndicator()
                    }
                }
                project == null -> {
                    ProjectNotFoundContent()
                }
                else -> {
                    ProjectDetailsContent(
                        project = project,
                        projectFiles = state.projectFiles,
                        userRole = userRole,
                        isAdminOrOrganizer = isAdminOrOrganizer,
                        isLeader = state.isLeaderOfProject(project),
                        isActionLoading = state.isActionLoading,
                        viewModel = viewModel,
                        onProjectDeleted = onBack
                    )
                }
            }
        }
    }

    if (showDisqualifyConfirm && project != null) {
        AlertDialog(
            onDismissRequest = { showDisqualifyConfirm = false },
            icon = {
                Icon(
                    Icons.Default.Block,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Дисквалифицировать проект?") },
            text = {
                Text("Проект будет дисквалифицирован и исключён из оценивания. Это действие необратимо.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.disqualifyProject(project.jamId, projectId)
                        showDisqualifyConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Дисквалифицировать") }
            },
            dismissButton = {
                TextButton(onClick = { showDisqualifyConfirm = false }) { Text("Отмена") }
            }
        )
    }
}



fun formatFileSize(bytes: Long): String = when {
    bytes < 1024L -> "$bytes Б"
    bytes < 1_048_576L -> "${bytes / 1024} КБ"
    bytes < 1_073_741_824L -> "${bytes / 1_048_576} МБ"
    else -> "${bytes / 1_073_741_824} ГБ"
}
