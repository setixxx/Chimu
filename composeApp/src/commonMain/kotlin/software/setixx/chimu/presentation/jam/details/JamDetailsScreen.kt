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
import software.setixx.chimu.domain.model.CreateRatingCriteria
import software.setixx.chimu.presentation.components.InfoRow
import software.setixx.chimu.presentation.components.StatusChip

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
    
    var judgeUserIdInput by remember { mutableStateOf("") }
    
    var criteriaName by remember { mutableStateOf("") }
    var criteriaDesc by remember { mutableStateOf("") }
    var criteriaMaxScore by remember { mutableStateOf("10") }
    var criteriaWeight by remember { mutableStateOf("1.0") }
    
    val snackbarHostState = remember { SnackbarHostState() }

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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Критерии оценивания", style = MaterialTheme.typography.titleMedium)
                                if (state.canEdit) {
                                    IconButton(onClick = { showAddCriteriaDialog = true }) {
                                        Icon(Icons.Default.Add, "Добавить критерий")
                                    }
                                }
                            }
                            if (state.criteria.isEmpty()) {
                                Text("Критерии еще не созданы", style = MaterialTheme.typography.bodySmall)
                            } else {
                                state.criteria.forEach { item ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(item.name, style = MaterialTheme.typography.bodyLarge)
                                            if (!item.description.isNullOrBlank()) {
                                                Text(item.description!!, style = MaterialTheme.typography.bodySmall)
                                            }
                                            Text("Макс. балл: ${item.maxScore}, Вес: ${item.weight}", style = MaterialTheme.typography.labelSmall)
                                        }
                                        if (state.canEdit) {
                                            IconButton(onClick = { viewModel.deleteCriteria(jamId, item.id) }) {
                                                Icon(Icons.Default.Delete, "Удалить", tint = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    }
                                    if (item != state.criteria.last()) HorizontalDivider()
                                }
                            }
                        }
                    }

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Судьи", style = MaterialTheme.typography.titleMedium)
                                if (state.canEdit) {
                                    IconButton(onClick = { showAssignJudgeDialog = true }) {
                                        Icon(Icons.Default.PersonAdd, "Назначить судью")
                                    }
                                }
                            }
                            if (state.judges.isEmpty()) {
                                Text("Судьи еще не назначены", style = MaterialTheme.typography.bodySmall)
                            } else {
                                state.judges.forEach { judge ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(judge.nickname, style = MaterialTheme.typography.bodyLarge)
                                        if (state.canEdit) {
                                            IconButton(onClick = { viewModel.unassignJudge(jamId, judge.userId) }) {
                                                Icon(Icons.Default.Close, "Удалить", tint = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    }
                                }
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

                    if (state.isParticipant && state.userTeams.isNotEmpty()) {
                        Button(
                            onClick = { showRegisterDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.isActionLoading
                        ) {
                            Text("Подать заявку на участие")
                        }
                    }

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Команды-участники (${state.registrations.size})", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            if (state.registrations.isEmpty()) {
                                Text("Пока нет зарегистрированных команд", style = MaterialTheme.typography.bodyMedium)
                            } else {
                                state.registrations.forEach { registration ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(registration.teamName, style = MaterialTheme.typography.bodyLarge)
                                            Text("От: ${registration.registeredByNickname}", style = MaterialTheme.typography.bodySmall)
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(registration.status, style = MaterialTheme.typography.bodySmall)
                                            if (state.canEdit && registration.status == "PENDING") {
                                                IconButton(onClick = { viewModel.updateRegistrationStatus(jamId, registration.teamId, "APPROVED") }) {
                                                    Icon(Icons.Default.Check, "Одобрить", tint = MaterialTheme.colorScheme.primary)
                                                }
                                                IconButton(onClick = { viewModel.updateRegistrationStatus(jamId, registration.teamId, "REJECTED") }) {
                                                    Icon(Icons.Default.Close, "Отклонить", tint = MaterialTheme.colorScheme.error)
                                                }
                                            }
                                            if (registration.registeredBy == state.userId && registration.status != "WITHDRAWN") {
                                                IconButton(onClick = { viewModel.withdrawTeam(jamId, registration.teamId) }) {
                                                    Icon(Icons.Default.ExitToApp, "Отозвать", tint = MaterialTheme.colorScheme.error)
                                                }
                                            }
                                        }
                                    }
                                    if (registration != state.registrations.last()) HorizontalDivider()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddCriteriaDialog) {
        AlertDialog(
            onDismissRequest = { showAddCriteriaDialog = false },
            title = { Text("Добавить критерий") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = criteriaName, onValueChange = { criteriaName = it }, label = { Text("Название") })
                    OutlinedTextField(value = criteriaDesc, onValueChange = { criteriaDesc = it }, label = { Text("Описание") })
                    OutlinedTextField(value = criteriaMaxScore, onValueChange = { criteriaMaxScore = it }, label = { Text("Макс. балл") })
                    OutlinedTextField(value = criteriaWeight, onValueChange = { criteriaWeight = it }, label = { Text("Вес (например 1.0)") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.createCriteria(
                        jamId,
                        CreateRatingCriteria(
                            name = criteriaName,
                            description = criteriaDesc.takeIf { it.isNotBlank() },
                            maxScore = criteriaMaxScore.toIntOrNull() ?: 10,
                            weight = criteriaWeight.toDoubleOrNull() ?: 1.0,
                            orderIndex = state.criteria.size
                        )
                    )
                    showAddCriteriaDialog = false
                    criteriaName = ""; criteriaDesc = ""
                }) {
                    Text("Создать")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCriteriaDialog = false }) { Text("Отмена") }
            }
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
                    judgeUserIdInput = ""
                }) {
                    Text("Назначить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAssignJudgeDialog = false }) {
                    Text("Отмена")
                }
            }
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
            text = { Text("Это действие нельзя будет отменить.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteJam(jamId)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}