package software.setixx.chimu.presentation.project

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import software.setixx.chimu.domain.model.GameJamDetails
import software.setixx.chimu.presentation.jam.details.project.ProjectViewModel
import software.setixx.chimu.presentation.project.components.ImagePlaceholderGallery
import software.setixx.chimu.presentation.project.components.ProjectEditDialog
import software.setixx.chimu.presentation.project.components.ProjectFilesSection
import software.setixx.chimu.presentation.project.components.ProjectHeaderCard

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProjectDetailsContent(
    jamId: String,
    jam: GameJamDetails,
    userRole: UserRole?,
    userId: String?,
    onNavigateToProject: (projectId: String, roleStr: String?, isAdminOrOrganizer: Boolean) -> Unit,
    viewModel: ProjectViewModel = koinViewModel(),
    paddingValues: PaddingValues = PaddingValues()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    val isAdminOrOrganizer = userRole == UserRole.ADMIN ||
            (userRole == UserRole.ORGANIZER && jam.organizerId == userId)
    val isParticipant = userRole == UserRole.PARTICIPANT

    LaunchedEffect(jamId) { viewModel.load(jamId, isAdminOrOrganizer) }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    val screenshotPicker = rememberFilePicker { fileUpload ->
        fileUpload?.let {
            state.userProject?.id?.let { id ->
                viewModel.uploadFile(id, it.copy(fileType = ProjectFileType.SCREENSHOT))
            }
        }
    }
    val buildPicker = rememberFilePicker { fileUpload ->
        fileUpload?.let {
            state.userProject?.id?.let { id ->
                viewModel.uploadFile(id, it.copy(fileType = ProjectFileType.BUILD))
            }
        }
    }
    val videoPicker = rememberFilePicker { fileUpload ->
        fileUpload?.let {
            state.userProject?.id?.let { id ->
                viewModel.uploadFile(id, it.copy(fileType = ProjectFileType.VIDEO))
            }
        }
    }

    var showCreateDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("") }
    var dialogDescription by remember { mutableStateOf("") }
    var dialogGameUrl by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LoadingIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isParticipant) {
                    val isLeader = state.isUserLeaderOfRegisteredTeam()
                    val isRegistered = state.getUserRegistration() != null

                    if (!isRegistered) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    "Вы не зарегистрированы на этот джем в составе команды",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    } else {
                        val project = state.userProject

                        if (project == null) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text("Проект", style = MaterialTheme.typography.titleLarge)
                                    Text(
                                        text = if (isLeader)
                                            "Проект ещё не создан. Создайте его, чтобы начать работу."
                                        else
                                            "Проект ещё не создан. Обратитесь к лидеру команды.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (isLeader) {
                                        Button(
                                            onClick = {
                                                dialogTitle = ""
                                                dialogDescription = ""
                                                dialogGameUrl = ""
                                                showCreateDialog = true
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null)
                                            Spacer(Modifier.width(8.dp))
                                            Text("Создать проект")
                                        }
                                    }
                                }
                            }
                        } else {
                            val isDraft = project.status == ProjectStatus.DRAFT.name
                            val isSubmitted = project.status == ProjectStatus.SUBMITTED.name
                            val canEdit = isLeader && isDraft && project.canEdit
                            val canUpload = isDraft && project.canEdit
                            val canDeleteFile = isLeader && isDraft && project.canEdit

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
                                        screenshotFiles = state.projectFiles.filter {
                                            it.fileType == ProjectFileType.SCREENSHOT
                                        },
                                        canUpload = canUpload,
                                        canDelete = canDeleteFile,
                                        isReadOnly = false,
                                        onUpload = { screenshotPicker() },
                                        onDelete = { fileId -> viewModel.deleteFile(project.id, fileId) }
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
                                        isReadOnly = false,
                                        onUpload = { buildPicker() },
                                        onDelete = { fileId -> viewModel.deleteFile(project.id, fileId) }
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
                                        isReadOnly = false,
                                        onUpload = { videoPicker() },
                                        onDelete = { fileId -> viewModel.deleteFile(project.id, fileId) }
                                    )
                                }
                            }

                            if (isLeader) {
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
                                        Text(
                                            "Управление",
                                            style = MaterialTheme.typography.titleSmall
                                        )

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
                                                onClick = { viewModel.cancelSubmission(project.id) },
                                                modifier = Modifier.fillMaxWidth(),
                                                enabled = !state.isActionLoading
                                            ) { Text("Отменить отправку") }
                                        } else if (isDraft) {
                                            if (project.canSubmit) {
                                                Button(
                                                    onClick = { viewModel.submitProject(project.id) },
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
                        }
                    }
                }

                if (isAdminOrOrganizer) {
                    AdminProjectListSection(
                        state = state,
                        userRole = userRole,
                        onNavigateToProject = onNavigateToProject
                    )
                }

                Spacer(Modifier.height(80.dp))
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (showCreateDialog) {
        ProjectEditDialog(
            title = "Создание проекта",
            projectTitle = dialogTitle,
            projectDescription = dialogDescription,
            projectGameUrl = dialogGameUrl,
            onTitleChange = { dialogTitle = it },
            onDescriptionChange = { dialogDescription = it },
            onGameUrlChange = { dialogGameUrl = it },
            onConfirm = {
                viewModel.createProject(
                    jamId,
                    dialogTitle,
                    dialogDescription.takeIf { it.isNotBlank() }
                )
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
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
                state.userProject?.id?.let { projectId ->
                    viewModel.updateProject(
                        projectId = projectId,
                        title = dialogTitle,
                        description = dialogDescription.takeIf { it.isNotBlank() },
                        gameUrl = dialogGameUrl.takeIf { it.isNotBlank() }
                    )
                }
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
            text = { Text("Это действие необратимо. Все файлы проекта будут удалены.") },
            confirmButton = {
                Button(
                    onClick = {
                        state.userProject?.id?.let { viewModel.deleteProject(it) }
                        showDeleteConfirm = false
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
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AdminProjectListSection(
    state: software.setixx.chimu.presentation.jam.details.project.ProjectState,
    userRole: UserRole?,
    onNavigateToProject: (String, String?, Boolean) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Проекты участников", style = MaterialTheme.typography.titleMedium)

            if (state.allProjects.isEmpty()) {
                Text(
                    "Проекты ещё не созданы",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                    state.allProjects.forEachIndexed { index, project ->
                        SegmentedListItem(
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                            selected = false,
                            onClick = { onNavigateToProject(project.id, userRole?.name, true) },
                            shapes = if (state.allProjects.size == 1)
                                ListItemDefaults.shapes(shape = MaterialTheme.shapes.medium)
                            else
                                ListItemDefaults.segmentedShapes(index, state.allProjects.size),
                            leadingContent = {
                                Icon(
                                    Icons.Default.Gamepad,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            content = { Text(project.title) },
                            supportingContent = {
                                Text(
                                    "Команда: ${project.teamName ?: "—"}  •  ${project.status.name}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            trailingContent = {
                                Icon(
                                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}