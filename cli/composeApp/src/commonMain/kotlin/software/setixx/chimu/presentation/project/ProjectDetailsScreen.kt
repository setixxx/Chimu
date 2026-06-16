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
import chimu.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
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
    val isDisqualified = project?.status == ProjectStatus.DISQUALIFIED
    var showDisqualifyConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(project?.title ?: stringResource(Res.string.project_label)) },
                navigationIcon = {
                    FilledTonalIconButton(
                        onClick = onBack
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.back))
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
            title = { Text(stringResource(Res.string.project_disqualify_title)) },
            text = {
                Text(stringResource(Res.string.project_disqualify_message))
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
                ) { Text(stringResource(Res.string.project_disqualify_button)) }
            },
            dismissButton = {
                TextButton(onClick = { showDisqualifyConfirm = false }) { Text(stringResource(Res.string.cancel)) }
            }
        )
    }
}

@Composable
fun formatFileSize(bytes: Long): String = when {
    bytes < 1024L -> stringResource(Res.string.file_size_b, bytes)
    bytes < 1_048_576L -> stringResource(Res.string.file_size_kb, bytes / 1024)
    bytes < 1_073_741_824L -> stringResource(Res.string.file_size_mb, bytes / 1_048_576)
    else -> stringResource(Res.string.file_size_gb, bytes / 1_073_741_824)
}
