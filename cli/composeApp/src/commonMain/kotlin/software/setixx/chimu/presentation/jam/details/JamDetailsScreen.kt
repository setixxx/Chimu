package software.setixx.chimu.presentation.jam.details

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
import software.setixx.chimu.api.domain.GameJamStatus
import software.setixx.chimu.api.domain.ProjectFileType
import software.setixx.chimu.api.domain.ProjectStatus
import software.setixx.chimu.data.picker.rememberFilePicker
import software.setixx.chimu.domain.model.CreateRatingCriteria
import software.setixx.chimu.presentation.components.InfoRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JamDetailsScreen(
    jamId: String,
    onBack: () -> Unit,
    onEditJam: (String) -> Unit,
    viewModel: JamDetailsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRegisterDialog by remember { mutableStateOf(false) }
    var showAssignJudgeDialog by remember { mutableStateOf(false) }
    var showAddCriteriaDialog by remember { mutableStateOf(false) }
    var showCreateProjectDialog by remember { mutableStateOf(false) }

    var judgeUserIdInput by remember { mutableStateOf("") }

    var criteriaName by remember { mutableStateOf("") }
    var criteriaDesc by remember { mutableStateOf("") }
    var criteriaMaxScore by remember { mutableStateOf("10") }
    var criteriaWeight by remember { mutableStateOf("1.0") }

    var projectTitle by remember { mutableStateOf("") }
    var projectDescription by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }

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
        viewModel.loadJamDetails(jamId)
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) {
            onBack()
        }
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
                            Icon(Icons.Default.Delete, "Удалить", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            state.jamDetails?.let { jam ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // COMMON INFO (Visible to everyone)
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Информация", style = MaterialTheme.typography.titleLarge)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(jam.description ?: "Нет описания")
                            if (jam.theme != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Тема: ${jam.theme}", style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Даты", style = MaterialTheme.typography.titleMedium)
                            InfoRow(icon = Icons.Default.AppRegistration, label = "Регистрация", value = "${jam.registrationStart} - ${jam.registrationEnd}")
                            InfoRow(icon = Icons.Default.PlayArrow, label = "Джем", value = "${jam.jamStart} - ${jam.jamEnd}")
                            InfoRow(icon = Icons.Default.Star, label = "Оценивание", value = "${jam.judgingStart} - ${jam.judgingEnd}")
                        }
                    }

                    // ROLE-BASED LOGIC
                    if (state.isJudge) {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(Icons.Default.Gavel, null, tint = MaterialTheme.colorScheme.primary)
                                Text("Вы участвуете как судья. Вы сможете оценивать проекты после окончания периода разработки.")
                            }
                        }
                    } else {
                        // PARTICIPANT SECTION
                        if (jam.status == GameJamStatus.IN_PROGRESS.name && state.isParticipant) {
                            val registration = state.getUserRegistration()
                            if (registration != null) {
                                ParticipantInProgressSection(
                                    state = state,
                                    onCreateProject = { showCreateProjectDialog = true },
                                    onSubmitProject = { viewModel.submitProject(it) },
                                    onCancelSubmission = { viewModel.cancelSubmission(it) },
                                    onUploadScreenshot = screenshotPicker,
                                    onUploadBuild = buildPicker,
                                    onUploadVideo = videoPicker,
                                    onDeleteFile = { viewModel.deleteFile(state.userProject?.id ?: "", it) }
                                )
                            }
                        }

                        if (jam.status == GameJamStatus.REGISTRATION_OPEN.name && state.isParticipant && state.userTeams.isNotEmpty()) {
                            Button(
                                onClick = { showRegisterDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !state.isActionLoading
                            ) {
                                Text("Подать заявку на участие")
                            }
                        }

                        // ADMIN / ORGANIZER SECTION
                        if (jam.status == GameJamStatus.IN_PROGRESS.name && state.isAdminOrOrganizer) {
                            AdminInProgressSection(
                                state = state,
                                onDisqualifyProject = { viewModel.disqualifyProject(jamId, it) }
                            )
                        }

                        // REGISTERED TEAMS (Visible to Participants, Admins, Organizers)
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Команды-участники (${state.registrations.size})", style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(8.dp))
                                if (state.registrations.isEmpty()) {
                                    Text("Пока нет зарегистрированных команд", style = MaterialTheme.typography.bodyMedium)
                                } else {
                                    state.registrations.forEach { reg ->
                                        ListItem(
                                            headlineContent = { Text(reg.teamName) },
                                            supportingContent = { Text("От: ${reg.registeredByNickname}") },
                                            trailingContent = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Badge { Text(reg.status) }
                                                    if (state.canEdit && reg.status == "PENDING") {
                                                        IconButton(onClick = { viewModel.updateRegistrationStatus(jamId, reg.teamId, "APPROVED") }) {
                                                            Icon(Icons.Default.Check, "Одобрить", tint = MaterialTheme.colorScheme.primary)
                                                        }
                                                        IconButton(onClick = { viewModel.updateRegistrationStatus(jamId, reg.teamId, "REJECTED") }) {
                                                            Icon(Icons.Default.Close, "Отклонить", tint = MaterialTheme.colorScheme.error)
                                                        }
                                                    }
                                                }
                                            }
                                        )
                                        if (reg != state.registrations.last()) HorizontalDivider()
                                    }
                                }
                            }
                        }
                    }

                    // MANAGE SECTION (Admins & Organizers)
                    if (state.isAdminOrOrganizer) {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Управление джемом", style = MaterialTheme.typography.titleMedium)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(onClick = { showAddCriteriaDialog = true }) {
                                    Icon(Icons.Default.Add, null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Добавить критерий оценивания")
                                }
                                TextButton(onClick = { showAssignJudgeDialog = true }) {
                                    Icon(Icons.Default.PersonAdd, null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Назначить судью")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // DIALOGS
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

    if (showAddCriteriaDialog) {
        AlertDialog(
            onDismissRequest = { showAddCriteriaDialog = false },
            title = { Text("Новый критерий") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = criteriaName, onValueChange = { criteriaName = it }, label = { Text("Название") })
                    OutlinedTextField(value = criteriaDesc, onValueChange = { criteriaDesc = it }, label = { Text("Описание") })
                    OutlinedTextField(value = criteriaMaxScore, onValueChange = { criteriaMaxScore = it }, label = { Text("Макс. балл") })
                    OutlinedTextField(value = criteriaWeight, onValueChange = { criteriaWeight = it }, label = { Text("Вес") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.createCriteria(
                        jamId,
                        CreateRatingCriteria(
                            criteriaName,
                            criteriaDesc.takeIf { it.isNotBlank() },
                            criteriaMaxScore.toIntOrNull() ?: 10,
                            criteriaWeight.toDoubleOrNull() ?: 1.0,
                            state.criteria.size
                        )
                    )
                    showAddCriteriaDialog = false
                }) { Text("Добавить") }
            },
            dismissButton = { TextButton(onClick = { showAddCriteriaDialog = false }) { Text("Отмена") } }
        )
    }

    if (showAssignJudgeDialog) {
        AlertDialog(
            onDismissRequest = { showAssignJudgeDialog = false },
            title = { Text("Назначить судью") },
            text = {
                OutlinedTextField(
                    value = judgeUserIdInput,
                    onValueChange = { judgeUserIdInput = it },
                    label = { Text("ID пользователя") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.assignJudge(jamId, judgeUserIdInput)
                    showAssignJudgeDialog = false
                }) { Text("Назначить") }
            },
            dismissButton = { TextButton(onClick = { showAssignJudgeDialog = false }) { Text("Отмена") } }
        )
    }

    if (showRegisterDialog) {
        AlertDialog(
            onDismissRequest = { showRegisterDialog = false },
            title = { Text("Выберите команду") },
            text = {
                Column {
                    state.userTeams.forEach { team ->
                        val isRegistered = state.isTeamRegistered(team.id)
                        ListItem(
                            headlineContent = { Text(team.name) },
                            supportingContent = { Text(if (isRegistered) "Уже зарегистрирована" else "${team.memberCount} участников") },
                            trailingContent = {
                                if (!isRegistered) {
                                    Button(onClick = {
                                        viewModel.registerTeam(jamId, team.id)
                                        showRegisterDialog = false
                                    }) {
                                        Text("Выбрать")
                                    }
                                }
                            }
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showRegisterDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить джем?") },
            text = { Text("Все данные, включая регистрации и проекты, будут безвозвратно удалены.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteJam(jamId); showDeleteDialog = false }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text("Удалить")
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Отмена") } }
        )
    }
}

@Composable
fun ParticipantInProgressSection(
    state: JamDetailsState,
    onCreateProject: () -> Unit,
    onSubmitProject: (String) -> Unit,
    onCancelSubmission: (String) -> Unit,
    onUploadScreenshot: () -> Unit,
    onUploadBuild: () -> Unit,
    onUploadVideo: () -> Unit,
    onDeleteFile: (String) -> Unit
) {
    val isLeader = state.isUserLeaderOfRegisteredTeam()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Ваш проект", style = MaterialTheme.typography.titleMedium)
            
            val project = state.userProject
            if (project == null) {
                Text("Проект еще не создан. Создать его может только лидер команды.")
                if (isLeader) {
                    Button(onClick = onCreateProject, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Add, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Создать проект")
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(project.title, style = MaterialTheme.typography.headlineSmall)
                    Text(project.description ?: "Нет описания", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    SuggestionChip(onClick = {}, label = { Text("Статус: ${project.status}") })
                }

                HorizontalDivider()

                Text("Файлы проекта", style = MaterialTheme.typography.labelLarge)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val screenshotsCount = state.projectFiles.count { it.fileType == ProjectFileType.SCREENSHOT }
                    val buildsCount = state.projectFiles.count { it.fileType == ProjectFileType.BUILD }
                    val videosCount = state.projectFiles.count { it.fileType == ProjectFileType.VIDEO }

                    FileUploadButton(Icons.Default.Image, "Изображения", screenshotsCount, 5, isLeader, onUploadScreenshot)
                    FileUploadButton(Icons.Default.Build, "Сборки", buildsCount, 5, isLeader, onUploadBuild)
                    FileUploadButton(Icons.Default.VideoLibrary, "Видео", videosCount, 5, isLeader, onUploadVideo)
                }

                if (state.projectFiles.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Список файлов", style = MaterialTheme.typography.labelMedium)
                    state.projectFiles.forEach { file ->
                        ListItem(
                            headlineContent = { Text(file.fileName) },
                            supportingContent = { Text("${file.fileType} • ${file.fileSize / 1024} KB") },
                            trailingContent = {
                                if (isLeader) {
                                    IconButton(onClick = { onDeleteFile(file.id) }) {
                                        Icon(Icons.Default.Delete, "Удалить", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (isLeader) {
                    if (project.status == ProjectStatus.DRAFT.name) {
                        Button(
                            onClick = { onSubmitProject(project.id) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.isActionLoading
                        ) {
                            Text("Отправить проект")
                        }
                    } else if (project.status == ProjectStatus.SUBMITTED.name) {
                        OutlinedButton(
                            onClick = { onCancelSubmission(project.id) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.isActionLoading
                        ) {
                            Text("Отменить отправку")
                        }
                    }
                } else {
                    Text("Только лидер может отправлять или изменять файлы проекта.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }
            }
        }
    }
}

@Composable
fun AdminInProgressSection(
    state: JamDetailsState,
    onDisqualifyProject: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Текущие проекты", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            
            if (state.allProjects.isEmpty()) {
                Text("Пока нет отправленных проектов.", style = MaterialTheme.typography.bodyMedium)
            } else {
                state.allProjects.forEach { project ->
                    ListItem(
                        headlineContent = { Text(project.title) },
                        supportingContent = { Text("Команда: ${project.teamName ?: "Без названия"}") },
                        trailingContent = {
                            if (project.status == ProjectStatus.SUBMITTED) {
                                IconButton(onClick = { onDisqualifyProject(project.id) }) {
                                    Icon(Icons.Default.Block, "Дисквалифицировать", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    )
                    if (project != state.allProjects.last()) HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun FileUploadButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    currentCount: Int,
    maxCount: Int,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(4.dp)) {
        Box(contentAlignment = Alignment.Center) {
            FilledIconButton(
                onClick = onClick,
                enabled = enabled && currentCount < maxCount,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(icon, contentDescription = null)
            }
            if (currentCount > 0) {
                Badge(modifier = Modifier.align(Alignment.TopEnd)) { Text(currentCount.toString()) }
            }
        }
        Text(label, style = MaterialTheme.typography.labelSmall)
        Text("$currentCount/$maxCount", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
    }
}