package software.setixx.chimu.presentation.project

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import software.setixx.chimu.api.domain.ProjectFileType
import software.setixx.chimu.api.domain.ProjectStatus
import software.setixx.chimu.api.domain.UserRole
import software.setixx.chimu.data.picker.rememberFilePicker
import software.setixx.chimu.presentation.jam.details.project.ProjectViewModel
import software.setixx.chimu.presentation.project.components.ImagePlaceholderGallery
import software.setixx.chimu.presentation.project.components.ProjectEditDialog
import software.setixx.chimu.presentation.project.components.ProjectFilesSection
import software.setixx.chimu.presentation.project.components.ProjectHeaderCard

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
    val scrollState = rememberScrollState()

    val isParticipant = userRole == UserRole.PARTICIPANT
    val isJudge = userRole == UserRole.JUDGE

    LaunchedEffect(projectId) { viewModel.loadProjectById(projectId) }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    val project = state.userProject
    val isLeader = project?.let { state.isLeaderOfProject(it) } ?: false
    val isDraft = project?.status == ProjectStatus.DRAFT.name
    val isSubmitted = project?.status == ProjectStatus.SUBMITTED.name
    val isDisqualified = project?.status == ProjectStatus.DISQUALIFIED.name

    val canEdit = isParticipant && isLeader && isDraft && (project?.canEdit == true)
    val canUpload = isParticipant && isDraft && (project?.canEdit == true)
    val canDeleteFile = isParticipant && isLeader && isDraft && (project?.canEdit == true)

    val screenshotPicker = rememberFilePicker { fileUpload ->
        fileUpload?.let { viewModel.uploadFile(projectId, it.copy(fileType = ProjectFileType.SCREENSHOT)) }
    }
    val buildPicker = rememberFilePicker { fileUpload ->
        fileUpload?.let { viewModel.uploadFile(projectId, it.copy(fileType = ProjectFileType.BUILD)) }
    }
    val videoPicker = rememberFilePicker { fileUpload ->
        fileUpload?.let { viewModel.uploadFile(projectId, it.copy(fileType = ProjectFileType.VIDEO)) }
    }

    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showDisqualifyConfirm by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("") }
    var dialogDescription by remember { mutableStateOf("") }
    var dialogGameUrl by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(project?.title ?: "Проект") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    if (isAdminOrOrganizer && !isDisqualified && project != null) {
                        TextButton(
                            onClick = { showDisqualifyConfirm = true },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                Icons.Default.Block,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Дисквалифицировать")
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
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.FolderOff,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Проект не найден",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ProjectHeaderCard(
                            project = project,
                            canEdit = canEdit,
                            onEdit = {
                                dialogTitle = project.title
                                dialogDescription = project.description ?: ""
                                dialogGameUrl = project.gameUrl ?: ""
                                showEditDialog = true
                            }
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                ImagePlaceholderGallery(
                                    projectId = projectId,
                                    screenshotFiles = state.projectFiles.filter {
                                        it.fileType == ProjectFileType.SCREENSHOT
                                    },
                                    canUpload = canUpload,
                                    canDelete = canDeleteFile,
                                    isReadOnly = isJudge || isAdminOrOrganizer,
                                    onUpload = { screenshotPicker() },
                                    onDelete = { fileId ->
                                        state.projectFiles.find { it.id == fileId }?.let {
                                            viewModel.deleteFile(projectId, it.id, it.fileType)
                                        }
                                    },
                                    onDownload = { fileId ->
                                        state.projectFiles.find { it.id == fileId }?.let {
                                            viewModel.downloadFile(projectId, it.id, it.fileType, it.fileName, it.mimeType)
                                        }
                                    }
                                )
                            }
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                ProjectFilesSection(
                                    title = "Сборки",
                                    icon = Icons.Default.Build,
                                    files = state.projectFiles.filter {
                                        it.fileType == ProjectFileType.BUILD
                                    },
                                    canUpload = canUpload,
                                    canDelete = canDeleteFile,
                                    isReadOnly = isJudge || isAdminOrOrganizer,
                                    onUpload = { buildPicker() },
                                    onDelete = { fileId ->
                                        state.projectFiles.find { it.id == fileId }?.let {
                                            viewModel.deleteFile(projectId, it.id, it.fileType)
                                        }
                                    },
                                    onDownload = { fileId ->
                                        state.projectFiles.find { it.id == fileId }?.let {
                                            viewModel.downloadFile(projectId, it.id, it.fileType, it.fileName, it.mimeType)
                                        }
                                    }
                                )
                            }
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                ProjectFilesSection(
                                    title = "Видео",
                                    icon = Icons.Default.VideoLibrary,
                                    files = state.projectFiles.filter {
                                        it.fileType == ProjectFileType.VIDEO
                                    },
                                    canUpload = canUpload,
                                    canDelete = canDeleteFile,
                                    isReadOnly = isJudge || isAdminOrOrganizer,
                                    onUpload = { videoPicker() },
                                    onDelete = { fileId ->
                                        state.projectFiles.find { it.id == fileId }?.let {
                                            viewModel.deleteFile(projectId, it.id, it.fileType)
                                        }
                                    },
                                    onDownload = { fileId ->
                                        state.projectFiles.find { it.id == fileId }?.let {
                                            viewModel.downloadFile(projectId, it.id, it.fileType, it.fileName, it.mimeType)
                                        }
                                    }
                                )
                            }
                        }

                        if (isParticipant && isLeader) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("Управление", style = MaterialTheme.typography.titleSmall)

                                    if (isSubmitted) {
                                        Surface(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = MaterialTheme.shapes.medium,
                                            color = MaterialTheme.colorScheme.tertiaryContainer
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                                                )
                                                Text(
                                                    "Проект отправлен на оценивание",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                                )
                                            }
                                        }
                                        OutlinedButton(
                                            onClick = { viewModel.cancelSubmission(projectId) },
                                            modifier = Modifier.fillMaxWidth(),
                                            enabled = !state.isActionLoading
                                        ) { Text("Отменить отправку") }
                                    } else if (isDraft) {
                                        if (project.canSubmit) {
                                            Button(
                                                onClick = { viewModel.submitProject(projectId) },
                                                modifier = Modifier.fillMaxWidth(),
                                                enabled = !state.isActionLoading
                                            ) { Text("Отправить проект") }
                                        }
                                        if (project.canDelete) {
                                            OutlinedButton(
                                                onClick = { showDeleteConfirm = true },
                                                modifier = Modifier.fillMaxWidth(),
                                                enabled = !state.isActionLoading,
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    contentColor = MaterialTheme.colorScheme.error
                                                ),
                                                border = BorderStroke(
                                                    1.dp,
                                                    MaterialTheme.colorScheme.error
                                                )
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = null)
                                                Spacer(Modifier.width(8.dp))
                                                Text("Удалить проект")
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        ProjectEditDialog(
            title = "Редактировать проект",
            projectTitle = dialogTitle,
            projectDescription = dialogDescription,
            projectGameUrl = dialogGameUrl,
            onTitleChange = { dialogTitle = it },
            onDescriptionChange = { dialogDescription = it },
            onGameUrlChange = { dialogGameUrl = it },
            onConfirm = {
                viewModel.updateProject(
                    projectId = projectId,
                    title = dialogTitle,
                    description = dialogDescription.takeIf { it.isNotBlank() },
                    gameUrl = dialogGameUrl.takeIf { it.isNotBlank() }
                )
                showEditDialog = false
            },
            onDismiss = { showEditDialog = false }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            icon = {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Удалить проект?") },
            text = { Text("Это действие необратимо.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProject(projectId)
                        showDeleteConfirm = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Отмена") }
            }
        )
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