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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import org.koin.compose.viewmodel.koinViewModel
import software.setixx.chimu.api.domain.GameJamStatus
import software.setixx.chimu.api.domain.RegistrationStatus
import software.setixx.chimu.data.picker.rememberFilePicker
import software.setixx.chimu.domain.model.CreateRatingCriteria
import software.setixx.chimu.domain.model.GameJamDetails
import software.setixx.chimu.domain.model.JamStatistics
import software.setixx.chimu.domain.model.Leaderboard
import software.setixx.chimu.domain.model.Project
import software.setixx.chimu.domain.model.Registration
import software.setixx.chimu.presentation.components.SegmentedListItemWithExpansion
import software.setixx.chimu.presentation.components.localizeStatus
import software.setixx.chimu.presentation.jam.details.management.components.JamStatisticsCard
import software.setixx.chimu.presentation.jam.details.management.components.LeaderboardCard
import software.setixx.chimu.presentation.jam.details.management.components.TeamsWithProjectsSection
import software.setixx.chimu.presentation.utils.DateTimeUtils

@Composable
fun ManagementScreen(
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
        fileUpload?.let { viewModel.uploadBanner(jam.id, it) }
    }
    
    val currentJam = state.jam ?: jam
    val isDraft = state.jam?.status == GameJamStatus.DRAFT && !state.isPublished
    val currentBannerUrl = currentJam.bannerUrl

    LaunchedEffect(jam.id) {
        viewModel.load(jam.id)
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
                                if (currentBannerUrl != null) "Баннер загружен."
                                else "Баннер не загружен.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (currentBannerUrl != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalPlatformContext.current)
                                    .data(currentBannerUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Баннер джема",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(MaterialTheme.shapes.medium),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { bannerPicker() }) {
                                Icon(Icons.Default.Upload, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (currentBannerUrl != null) "Заменить" else "Загрузить")
                            }
                            if (currentBannerUrl != null) {
                                OutlinedButton(
                                    onClick = { viewModel.deleteBanner(jam.id) },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Удалить")
                                }
                            }
                        }
                    }
                }
            }

            state.statistics?.let { stats ->
                JamStatisticsCard(statistics = stats)
            }

            state.leaderboard?.let { lb ->
                if (lb.rankings.isNotEmpty()) {
                    LeaderboardCard(leaderboard = lb)
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Судьи (${state.judges.size})",
                            style = MaterialTheme.typography.titleMedium
                        )
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
                                supportingContent = {
                                    Text("Назначен: ${DateTimeUtils.formatDateTime(judge.assignedAt)}")
                                },
                                trailingContent = {
                                    IconButton(
                                        onClick = { viewModel.unassignJudge(jam.id, judge.userId) }
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

            TeamsWithProjectsSection(
                registrations = state.registrations,
                projectsByTeam = state.projectsByTeam,
                isActionLoading = state.isActionLoading,
                onApprove = { teamId ->
                    viewModel.updateRegistrationStatus(jam.id, teamId, RegistrationStatus.APPROVED)
                },
                onReject = { teamId ->
                    viewModel.updateRegistrationStatus(jam.id, teamId, RegistrationStatus.REJECTED)
                },
                onDisqualify = { teamId ->
                    viewModel.updateRegistrationStatus(jam.id, teamId, RegistrationStatus.DISQUALIFIED)
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
                                        onClick = { viewModel.deleteCriteria(jam.id, criteria.id) }
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

            if (isDraft) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { viewModel.publishJam(jam.id) },
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

            Spacer(modifier = Modifier.height(80.dp))
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
                    viewModel.assignJudge(jam.id, judgeUserIdInput)
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
                        jam.id,
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
