package software.setixx.chimu.presentation.jam.details.progress

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import software.setixx.chimu.api.domain.ProjectFileType
import software.setixx.chimu.api.domain.ProjectStatus
import software.setixx.chimu.api.domain.RegistrationStatus
import software.setixx.chimu.api.domain.UserRole
import software.setixx.chimu.data.picker.rememberFilePicker
import software.setixx.chimu.domain.model.GameJamDetails
import software.setixx.chimu.presentation.jam.details.progress.components.FileUploadButton
import software.setixx.chimu.presentation.components.JamOverviewSection
import software.setixx.chimu.presentation.components.RegisteredTeamsSection
import software.setixx.chimu.presentation.components.StagePlaceholder

@Composable
fun ProgressScreen(
    jamId: String,
    jam: GameJamDetails,
    userRole: UserRole?,
    userId: String?,
    viewModel: ProgressViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    var showCreateProjectDialog by remember { mutableStateOf(false) }
    var projectTitle by remember { mutableStateOf("") }
    var projectDescription by remember { mutableStateOf("") }

    val isAdminOrOrganizer = userRole == UserRole.ADMIN ||
            (userRole == UserRole.ORGANIZER && jam.organizerId == userId)
    val isParticipant = userRole == UserRole.PARTICIPANT
    val isJudge = userRole == UserRole.JUDGE

    val screenshotPicker = rememberFilePicker { fileUpload ->
        fileUpload?.let {
            state.userProject?.id?.let { projectId ->
                viewModel.uploadFile(projectId, it.copy(fileType = ProjectFileType.SCREENSHOT))
            }
        }
    }

    val buildPicker = rememberFilePicker { fileUpload ->
        fileUpload?.let {
            state.userProject?.id?.let { projectId ->
                viewModel.uploadFile(projectId, it.copy(fileType = ProjectFileType.BUILD))
            }
        }
    }

    val videoPicker = rememberFilePicker { fileUpload ->
        fileUpload?.let {
            state.userProject?.id?.let { projectId ->
                viewModel.uploadFile(projectId, it.copy(fileType = ProjectFileType.VIDEO))
            }
        }
    }

    LaunchedEffect(jamId) {
        viewModel.load(jamId, isAdminOrOrganizer)
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    if (showCreateProjectDialog) {
        AlertDialog(
            onDismissRequest = { showCreateProjectDialog = false },
            title = { Text("Создание проекта") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = projectTitle,
                        onValueChange = { projectTitle = it },
                        label = { Text("Название проекта") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = projectDescription,
                        onValueChange = { projectDescription = it },
                        label = { Text("Описание (необязательно)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.createProject(jamId, projectTitle, projectDescription)
                    showCreateProjectDialog = false
                }) { Text("Создать") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateProjectDialog = false }) { Text("Отмена") }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                JamOverviewSection(jam = jam)

                RegisteredTeamsSection(
                    registrations = if (isAdminOrOrganizer) {
                        state.registrations
                    } else {
                        state.registrations.filter { it.status == RegistrationStatus.APPROVED }
                    },
                    actions = { reg ->
                        if (isAdminOrOrganizer && reg.status != RegistrationStatus.DISQUALIFIED) {
                            IconButton(
                                onClick = { viewModel.disqualifyTeam(jamId, reg.teamId) },
                                enabled = !state.isActionLoading
                            ) {
                                Icon(
                                    Icons.Default.Block,
                                    "Дисквалифицировать команду",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                )

                if (isParticipant) {
                    val registration = state.getUserRegistration()
                    val isLeader = state.isUserLeaderOfRegisteredTeam()

                    if (registration != null) {
                        ParticipantProjectSection(
                            state = state,
                            isLeader = isLeader,
                            onCreateProject = { showCreateProjectDialog = true },
                            onSubmitProject = { viewModel.submitProject(it) },
                            onCancelSubmission = { viewModel.cancelSubmission(it) },
                            onDeleteProject = { viewModel.deleteProject(it) },
                            onUploadScreenshot = screenshotPicker,
                            onUploadBuild = buildPicker,
                            onUploadVideo = videoPicker,
                            onDeleteFile = { fileId ->
                                state.userProject?.id?.let {
                                    viewModel.deleteFile(it, fileId)
                                }
                            }
                        )
                    } else {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                "Ваша команда не зарегистрирована или заявка не одобрена.",
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }

                if (isJudge) {
                    StagePlaceholder("Оценка проектов еще не началась. Дождитесь стадии оценивания.")
                }

                if (isAdminOrOrganizer) {
                    StatisticsSection(state)
                    AdminProjectsSection(
                        state = state,
                        onDisqualify = { viewModel.disqualifyProject(jamId, it) }
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun ParticipantProjectSection(
    state: ProgressState,
    isLeader: Boolean,
    onCreateProject: () -> Unit,
    onSubmitProject: (String) -> Unit,
    onCancelSubmission: (String) -> Unit,
    onDeleteProject: (String) -> Unit,
    onUploadScreenshot: () -> Unit,
    onUploadBuild: () -> Unit,
    onUploadVideo: () -> Unit,
    onDeleteFile: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Ваш проект", style = MaterialTheme.typography.titleMedium)

            val project = state.userProject
            if (project == null) {
                Text("Проект ещё не создан. Создать его может только лидер команды.")
                if (isLeader) {
                    Button(
                        onClick = onCreateProject,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Создать проект")
                    }
                }
            } else {
                Text(project.title, style = MaterialTheme.typography.headlineSmall)
                Text(project.description ?: "Нет описания")
                SuggestionChip(onClick = {}, label = { Text("Статус: ${project.status}") })

                HorizontalDivider()

                Text("Файлы проекта", style = MaterialTheme.typography.labelLarge)
                val canEditDraft = isLeader && project.status == ProjectStatus.DRAFT.name

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    FileUploadButton(
                        icon = Icons.Default.Image,
                        label = "Скриншоты",
                        currentCount = state.projectFiles.count { it.fileType == ProjectFileType.SCREENSHOT },
                        maxCount = 5,
                        enabled = canEditDraft,
                        onClick = onUploadScreenshot
                    )
                    FileUploadButton(
                        icon = Icons.Default.Build,
                        label = "Сборки",
                        currentCount = state.projectFiles.count { it.fileType == ProjectFileType.BUILD },
                        maxCount = 5,
                        enabled = canEditDraft,
                        onClick = onUploadBuild
                    )
                    FileUploadButton(
                        icon = Icons.Default.VideoLibrary,
                        label = "Видео",
                        currentCount = state.projectFiles.count { it.fileType == ProjectFileType.VIDEO },
                        maxCount = 5,
                        enabled = canEditDraft,
                        onClick = onUploadVideo
                    )
                }

                if (state.projectFiles.isNotEmpty()) {
                    state.projectFiles.forEach { file ->
                        ListItem(
                            headlineContent = { Text(file.fileName) },
                            supportingContent = { Text("${file.fileType} • ${file.fileSize / 1024} KB") },
                            trailingContent = {
                                if (canEditDraft) {
                                    IconButton(onClick = { onDeleteFile(file.id) }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            "Удалить",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        )
                    }
                }

                if (isLeader) {
                    when (project.status) {
                        ProjectStatus.DRAFT.name -> {
                            Button(
                                onClick = { onSubmitProject(project.id) },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !state.isActionLoading
                            ) {
                                Text("Отправить проект")
                            }
                        }
                        ProjectStatus.SUBMITTED.name -> {
                            OutlinedButton(
                                onClick = { onCancelSubmission(project.id) },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !state.isActionLoading
                            ) {
                                Text("Отменить отправку")
                            }
                        }
                    }
                    if (project.status == ProjectStatus.DRAFT.name) {
                        OutlinedButton(
                            onClick = { onDeleteProject(project.id) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.isActionLoading,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.Delete, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Удалить проект")
                        }
                    }
                } else {
                    Text(
                        "Только лидер может управлять проектом.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
private fun StatisticsSection(state: ProgressState) {
    val statistics = state.statistics ?: return

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Статистика джема", style = MaterialTheme.typography.titleMedium)
            Text("Всего проектов: ${statistics.totalProjects}")
            Text("Отправлено: ${statistics.submittedProjects}")
            Text("Дисквалифицировано: ${statistics.disqualifiedProjects}")
            Text("Судей: ${statistics.totalJudges}")

            if (statistics.judgeCompletionRate.isNotEmpty()) {
                HorizontalDivider()
                Text("Прогресс судей", style = MaterialTheme.typography.labelLarge)
                statistics.judgeCompletionRate.forEach { judge ->
                    Text("${judge.judgeNickname}: ${judge.ratedProjects}/${judge.totalProjects} (${judge.completionPercentage}%)")
                }
            }
        }
    }
}

@Composable
private fun AdminProjectsSection(
    state: ProgressState,
    onDisqualify: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Текущие проекты", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))

            if (state.allProjects.isEmpty()) {
                Text("Пока нет проектов.")
            } else {
                state.allProjects.forEachIndexed { index, project ->
                    ListItem(
                        headlineContent = { Text(project.title) },
                        supportingContent = {
                            Text("Команда: ${project.teamName ?: "Без названия"}\nСтатус: ${project.status}")
                        },
                        trailingContent = {
                            if (project.status != ProjectStatus.DISQUALIFIED) {
                                IconButton(onClick = { onDisqualify(project.id) }) {
                                    Icon(
                                        Icons.Default.Block,
                                        "Дисквалифицировать",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    )
                    if (index < state.allProjects.lastIndex) HorizontalDivider()
                }
            }
        }
    }
}
