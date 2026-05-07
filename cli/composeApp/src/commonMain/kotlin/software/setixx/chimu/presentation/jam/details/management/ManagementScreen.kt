package software.setixx.chimu.presentation.jam.details.management

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
import software.setixx.chimu.api.domain.RegistrationStatus
import software.setixx.chimu.data.picker.rememberFilePicker
import software.setixx.chimu.domain.model.CreateRatingCriteria
import software.setixx.chimu.domain.model.GameJamDetails
import software.setixx.chimu.presentation.components.RegisteredTeamsSection
import software.setixx.chimu.presentation.utils.DateTimeUtils

@Composable
fun ManagementScreen(
    jamId: String,
    jam: GameJamDetails,
    viewModel: ManagementViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    var showAssignJudgeDialog by remember { mutableStateOf(false) }
    var showAddCriteriaDialog by remember { mutableStateOf(false) }

    var judgeUserIdInput by remember { mutableStateOf("") }
    var criteriaName by remember { mutableStateOf("") }
    var criteriaDesc by remember { mutableStateOf("") }
    var criteriaMaxScore by remember { mutableStateOf("10") }
    var criteriaWeight by remember { mutableStateOf("1.0") }

    val bannerPicker = rememberFilePicker { fileUpload ->
        fileUpload?.let { viewModel.uploadBanner(jamId, it) }
    }
    val isDraft = jam.status == GameJamStatus.DRAFT && !state.isPublished

    LaunchedEffect(jamId) {
        viewModel.load(jamId)
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccess()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column {
                            Text("Баннер джема", style = MaterialTheme.typography.titleMedium)
                            Text(
                                if (state.hasBanner) "Баннер загружен." else "Загрузите png, webp или jpg.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = bannerPicker,
                                enabled = !state.isActionLoading
                            ) {
                                Icon(Icons.Default.Image, null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (state.hasBanner) "Заменить" else "Загрузить")
                            }
                            if (state.hasBanner) {
                                IconButton(
                                    onClick = { viewModel.deleteBanner(jamId) },
                                    enabled = !state.isActionLoading
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        "Удалить баннер",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
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
                        Text("Судьи (${state.judges.size})", style = MaterialTheme.typography.titleMedium)
                        TextButton(onClick = { showAssignJudgeDialog = true }) {
                            Icon(Icons.Default.PersonAdd, null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Назначить")
                        }
                    }

                    if (state.judges.isEmpty()) {
                        Text("Судьи не назначены.", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        state.judges.forEachIndexed { index, judge ->
                            ListItem(
                                headlineContent = { Text(judge.nickname) },
                                supportingContent = { Text("Назначен: ${DateTimeUtils.formatDateTime(judge.assignedAt)}") },
                                trailingContent = {
                                    IconButton(
                                        onClick = { viewModel.unassignJudge(jamId, judge.userId) }
                                    ) {
                                        Icon(
                                            Icons.Default.PersonRemove,
                                            "Снять",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            )
                            if (index < state.judges.lastIndex) HorizontalDivider()
                        }
                    }
                }
            }

            RegisteredTeamsSection(
                registrations = state.registrations,
                title = "Заявки и команды",
                actions = { reg ->
                    Row {
                        if (reg.status == RegistrationStatus.PENDING) {
                            IconButton(
                                onClick = {
                                    viewModel.updateRegistrationStatus(jamId, reg.teamId,
                                        RegistrationStatus.APPROVED)
                                },
                                enabled = !state.isActionLoading
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    "Одобрить",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(
                                onClick = {
                                    viewModel.updateRegistrationStatus(jamId, reg.teamId, RegistrationStatus.REJECTED)
                                },
                                enabled = !state.isActionLoading
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    "Отклонить",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        if (reg.status != RegistrationStatus.DISQUALIFIED) {
                            IconButton(
                                onClick = {
                                    viewModel.updateRegistrationStatus(jamId, reg.teamId, RegistrationStatus.DISQUALIFIED)
                                },
                                enabled = !state.isActionLoading
                            ) {
                                Icon(
                                    Icons.Default.Block,
                                    "Дисквалифицировать",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Критерии оценивания (${state.criteria.size})",
                            style = MaterialTheme.typography.titleMedium
                        )
                        TextButton(onClick = { showAddCriteriaDialog = true }) {
                            Icon(Icons.Default.Add, null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Добавить")
                        }
                    }

                    if (state.criteria.isEmpty()) {
                        Text("Критерии не добавлены.", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        state.criteria.sortedBy { it.orderIndex }.forEachIndexed { index, criteria ->
                            ListItem(
                                headlineContent = { Text(criteria.name) },
                                supportingContent = {
                                    Text("Макс: ${criteria.maxScore} • Вес: ${criteria.weight}")
                                },
                                trailingContent = {
                                    IconButton(
                                        onClick = { viewModel.deleteCriteria(jamId, criteria.id) }
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            "Удалить",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            )
                            if (index < state.criteria.lastIndex) HorizontalDivider()
                        }
                    }
                }
            }
            if (isDraft){
                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { viewModel.publishJam(jamId) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !state.isLoading
                    ) {
                        if (state.isLoading) {
                            LoadingIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Опубликовать джем")
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
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
                    judgeUserIdInput = ""
                    showAssignJudgeDialog = false
                }) { Text("Назначить") }
            },
            dismissButton = {
                TextButton(onClick = { showAssignJudgeDialog = false }) { Text("Отмена") }
            }
        )
    }

    if (showAddCriteriaDialog) {
        AlertDialog(
            onDismissRequest = { showAddCriteriaDialog = false },
            title = { Text("Новый критерий") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = criteriaName,
                        onValueChange = { criteriaName = it },
                        label = { Text("Название") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = criteriaDesc,
                        onValueChange = { criteriaDesc = it },
                        label = { Text("Описание") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = criteriaMaxScore,
                        onValueChange = { criteriaMaxScore = it },
                        label = { Text("Макс. балл") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = criteriaWeight,
                        onValueChange = { criteriaWeight = it },
                        label = { Text("Вес") },
                        modifier = Modifier.fillMaxWidth()
                    )
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
                    criteriaName = ""
                    criteriaDesc = ""
                    criteriaMaxScore = "10"
                    criteriaWeight = "1.0"
                    showAddCriteriaDialog = false
                }) { Text("Добавить") }
            },
            dismissButton = {
                TextButton(onClick = { showAddCriteriaDialog = false }) { Text("Отмена") }
            }
        )
    }
}
