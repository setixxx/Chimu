package software.setixx.chimu.presentation.project.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import software.setixx.chimu.api.domain.ProjectFileType
import software.setixx.chimu.api.domain.ProjectStatus
import software.setixx.chimu.api.domain.UserRole
import software.setixx.chimu.data.picker.rememberFilePicker
import software.setixx.chimu.domain.model.ProjectDetails
import software.setixx.chimu.domain.model.ProjectFile
import software.setixx.chimu.presentation.jam.details.project.ProjectViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProjectDetailsContent(
    project: ProjectDetails,
    projectFiles: List<ProjectFile>,
    userRole: UserRole?,
    isAdminOrOrganizer: Boolean,
    isLeader: Boolean,
    isActionLoading: Boolean,
    viewModel: ProjectViewModel,
    modifier: Modifier = Modifier,
    onProjectDeleted: (() -> Unit)? = null
) {
    val scrollState = rememberScrollState()
    val isParticipant = userRole == UserRole.PARTICIPANT
    val isJudge = userRole == UserRole.JUDGE
    val isDraft = project.status == ProjectStatus.DRAFT.name
    val isSubmitted = project.status == ProjectStatus.SUBMITTED.name
    val canEdit = isParticipant && isLeader && isDraft && project.canEdit
    val canUpload = isParticipant && isDraft && project.canEdit
    val canDeleteFile = isParticipant && isLeader && isDraft && project.canEdit
    val isReadOnly = isJudge || isAdminOrOrganizer

    val screenshotPicker = rememberFilePicker { fileUpload ->
        fileUpload?.let { viewModel.uploadFile(project.id, it.copy(fileType = ProjectFileType.SCREENSHOT)) }
    }
    val buildPicker = rememberFilePicker { fileUpload ->
        fileUpload?.let { viewModel.uploadFile(project.id, it.copy(fileType = ProjectFileType.BUILD)) }
    }
    val videoPicker = rememberFilePicker { fileUpload ->
        fileUpload?.let { viewModel.uploadFile(project.id, it.copy(fileType = ProjectFileType.VIDEO)) }
    }

    var showEditDialog by remember(project.id) { mutableStateOf(false) }
    var showDeleteConfirm by remember(project.id) { mutableStateOf(false) }
    var dialogTitle by remember(project.id) { mutableStateOf(project.title) }
    var dialogDescription by remember(project.id) { mutableStateOf(project.description ?: "") }
    var dialogGameUrl by remember(project.id) { mutableStateOf(project.gameUrl ?: "") }

    Column(
        modifier = modifier
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
            colors = CardDefaults.cardColors(),
            shape = MaterialTheme.shapes.extraLarge,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ImagePlaceholderGallery(
                    projectId = project.id,
                    screenshotFiles = projectFiles.filter { it.fileType == ProjectFileType.SCREENSHOT },
                    canUpload = canUpload,
                    canDelete = canDeleteFile,
                    isReadOnly = isReadOnly,
                    onUpload = { screenshotPicker() },
                    onDelete = { fileId ->
                        projectFiles.find { it.id == fileId }?.let {
                            viewModel.deleteFile(project.id, it.id, it.fileType)
                        }
                    },
                    onDownload = { fileId ->
                        projectFiles.find { it.id == fileId }?.let {
                            viewModel.downloadFile(project.id, it.id, it.fileType, it.fileName, it.mimeType)
                        }
                    }
                )
            }
        }

        ProjectFilesCard(
            title = "Сборки",
            icon = Icons.Default.Build,
            files = projectFiles.filter { it.fileType == ProjectFileType.BUILD },
            canUpload = canUpload,
            canDelete = canDeleteFile,
            isReadOnly = isReadOnly,
            onUpload = { buildPicker() },
            onDelete = { fileId ->
                projectFiles.find { it.id == fileId }?.let {
                    viewModel.deleteFile(project.id, it.id, it.fileType)
                }
            },
            onDownload = { fileId ->
                projectFiles.find { it.id == fileId }?.let {
                    viewModel.downloadFile(project.id, it.id, it.fileType, it.fileName, it.mimeType)
                }
            }
        )

        ProjectFilesCard(
            title = "Видео",
            icon = Icons.Default.VideoLibrary,
            files = projectFiles.filter { it.fileType == ProjectFileType.VIDEO },
            canUpload = canUpload,
            canDelete = canDeleteFile,
            isReadOnly = isReadOnly,
            onUpload = { videoPicker() },
            onDelete = { fileId ->
                projectFiles.find { it.id == fileId }?.let {
                    viewModel.deleteFile(project.id, it.id, it.fileType)
                }
            },
            onDownload = { fileId ->
                projectFiles.find { it.id == fileId }?.let {
                    viewModel.downloadFile(project.id, it.id, it.fileType, it.fileName, it.mimeType)
                }
            }
        )

        if (isParticipant && isLeader) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(ButtonDefaults.MediumContainerHeight),
                        enabled = !isActionLoading
                    ) { Text("Отменить отправку") }
                } else if (isDraft) {
                    if (project.canSubmit) {
                        Button(
                            onClick = { viewModel.submitProject(project.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(ButtonDefaults.MediumContainerHeight),
                            enabled = !isActionLoading
                        ) { Text("Отправить проект") }
                    }
                    if (project.canDelete) {
                        OutlinedButton(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(ButtonDefaults.MediumContainerHeight),
                            enabled = !isActionLoading,
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

        Spacer(Modifier.height(80.dp))
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
                    projectId = project.id,
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
            text = { Text("Это действие необратимо. Все файлы проекта будут удалены.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProject(project.id)
                        showDeleteConfirm = false
                        onProjectDeleted?.invoke()
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