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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

            // ── Banner card ─────────────────────────────────────────────────
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
                                if (state.hasBanner) "Баннер загружен."
                                else "Баннер не загружен.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { bannerPicker() }) {
                                Icon(Icons.Default.Upload, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (state.hasBanner) "Заменить" else "Загрузить")
                            }
                            if (state.hasBanner) {
                                OutlinedButton(
                                    onClick = { viewModel.deleteBanner(jamId) },
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

            // ── Statistics card (shown when available) ──────────────────────
            state.statistics?.let { stats ->
                JamStatisticsCard(statistics = stats)
            }

            // ── Leaderboard card (shown when available) ─────────────────────
            state.leaderboard?.let { lb ->
                if (lb.rankings.isNotEmpty()) {
                    LeaderboardCard(leaderboard = lb)
                }
            }

            // ── Judges card ─────────────────────────────────────────────────
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

            // ── Teams & Projects expandable section ─────────────────────────
            TeamsWithProjectsSection(
                registrations = state.registrations,
                projectsByTeam = state.projectsByTeam,
                isActionLoading = state.isActionLoading,
                onApprove = { teamId ->
                    viewModel.updateRegistrationStatus(jamId, teamId, RegistrationStatus.APPROVED)
                },
                onReject = { teamId ->
                    viewModel.updateRegistrationStatus(jamId, teamId, RegistrationStatus.REJECTED)
                },
                onDisqualify = { teamId ->
                    viewModel.updateRegistrationStatus(jamId, teamId, RegistrationStatus.DISQUALIFIED)
                }
            )

            // ── Criteria card ───────────────────────────────────────────────
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

            // ── Publish button ──────────────────────────────────────────────
            if (isDraft) {
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

            Spacer(modifier = Modifier.height(80.dp))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    // ── Dialogs ─────────────────────────────────────────────────────────────

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

// ── Statistics Card ──────────────────────────────────────────────────────────

@Composable
private fun JamStatisticsCard(statistics: JamStatistics) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.BarChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Статистика джема",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Key numbers row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatChip(label = "Проектов", value = statistics.totalProjects.toString())
                StatChip(label = "Подано", value = statistics.submittedProjects.toString())
                StatChip(
                    label = "Дисквалиф.",
                    value = statistics.disqualifiedProjects.toString(),
                    isWarning = statistics.disqualifiedProjects > 0
                )
                StatChip(label = "Судей", value = statistics.totalJudges.toString())
            }

            // Average scores per criteria
            if (statistics.averageScoresPerCriteria.isNotEmpty()) {
                HorizontalDivider()
                Text(
                    "Средние оценки",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                statistics.averageScoresPerCriteria.forEach { criteria ->
                    val avg = criteria.averageScore.toFloatOrNull() ?: 0f
                    val max = criteria.maxScore.toFloat()
                    val progress = if (max > 0) avg / max else 0f
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                criteria.criteriaName,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                "${criteria.averageScore} / ${criteria.maxScore}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Judge completion rates
            if (statistics.judgeCompletionRate.isNotEmpty()) {
                HorizontalDivider()
                Text(
                    "Прогресс судей",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                statistics.judgeCompletionRate.forEach { judge ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(judge.judgeNickname, style = MaterialTheme.typography.bodySmall)
                            Text(
                                "${judge.ratedProjects}/${judge.totalProjects} проектов",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            color = when {
                                judge.completionPercentage == 100 -> MaterialTheme.colorScheme.primaryContainer
                                judge.completionPercentage > 50 -> MaterialTheme.colorScheme.secondaryContainer
                                else -> MaterialTheme.colorScheme.errorContainer
                            },
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                "${judge.completionPercentage}%",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatChip(label: String, value: String, isWarning: Boolean = false) {
    Surface(
        color = if (isWarning && value != "0")
            MaterialTheme.colorScheme.errorContainer
        else
            MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isWarning && value != "0")
                    MaterialTheme.colorScheme.onErrorContainer
                else
                    MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

// ── Leaderboard Card ─────────────────────────────────────────────────────────

@Composable
private fun LeaderboardCard(leaderboard: Leaderboard) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Таблица лидеров",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    "${leaderboard.qualifiedProjects} / ${leaderboard.totalProjects} оценено",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            leaderboard.rankings.take(10).forEachIndexed { index, ranking ->
                val isTop3 = ranking.rank <= 3
                val rankColor = when (ranking.rank) {
                    1 -> MaterialTheme.colorScheme.tertiary
                    2 -> MaterialTheme.colorScheme.secondary
                    3 -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "#${ranking.rank}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (isTop3) FontWeight.Bold else FontWeight.Normal,
                        color = rankColor,
                        modifier = Modifier.width(36.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            ranking.project.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isTop3) FontWeight.SemiBold else FontWeight.Normal
                        )
                        ranking.project.teamName?.let { team ->
                            Text(
                                team,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        ranking.score.total,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (index < leaderboard.rankings.lastIndex && index < 9) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                }
            }

            if (leaderboard.rankings.size > 10) {
                Text(
                    "...и ещё ${leaderboard.rankings.size - 10} проектов",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ── Teams with Projects Section ──────────────────────────────────────────────

@Composable
private fun TeamsWithProjectsSection(
    registrations: List<Registration>,
    projectsByTeam: Map<String, List<Project>>,
    isActionLoading: Boolean,
    onApprove: (teamId: String) -> Unit,
    onReject: (teamId: String) -> Unit,
    onDisqualify: (teamId: String) -> Unit
) {
    val visibleRegistrations = registrations.filter {
        it.status != RegistrationStatus.WITHDRAWN && it.status != RegistrationStatus.CANCELLED
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Заявки и проекты (${visibleRegistrations.size})",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (visibleRegistrations.isEmpty()) {
                Text(
                    "Пока нет зарегистрированных команд",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                visibleRegistrations.forEachIndexed { index, reg ->
                    val teamProjects = projectsByTeam[reg.teamId] ?: emptyList()
                    SegmentedListItemWithExpansion(
                        headline = reg.teamName,
                        supporting = "Зарегистрировал: ${reg.registeredByNickname}",
                        trailingContent = {
                            // Status badge
                            Surface(
                                color = registrationStatusColor(reg.status),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    localizeStatus(reg.status),
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        },
                        initiallyExpanded = false
                    ) {
                        // Action buttons row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (reg.status == RegistrationStatus.PENDING) {
                                OutlinedButton(
                                    onClick = { onApprove(reg.teamId) },
                                    enabled = !isActionLoading,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Одобрить")
                                }
                                OutlinedButton(
                                    onClick = { onReject(reg.teamId) },
                                    enabled = !isActionLoading,
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Отклонить")
                                }
                            }
                            if (reg.status == RegistrationStatus.APPROVED) {
                                OutlinedButton(
                                    onClick = { onDisqualify(reg.teamId) },
                                    enabled = !isActionLoading,
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Icon(
                                        Icons.Default.Block,
                                        null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Дисквалифицировать")
                                }
                            }
                        }

                        // Team projects
                        Text(
                            "Проекты команды:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        if (teamProjects.isEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Icon(
                                    Icons.Default.FolderOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "Проект ещё не подан",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            teamProjects.forEach { project ->
                                TeamProjectItem(project = project)
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }

                    if (index < visibleRegistrations.lastIndex) {
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun TeamProjectItem(project: Project) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Gamepad,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    project.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                project.description?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
                project.gameUrl?.let { url ->
                    Text(
                        url,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            // Project status badge
            Surface(
                color = when (project.status.name) {
                    "SUBMITTED" -> MaterialTheme.colorScheme.primaryContainer
                    "DISQUALIFIED" -> MaterialTheme.colorScheme.errorContainer
                    "UNDER_REVIEW" -> MaterialTheme.colorScheme.tertiaryContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                },
                shape = MaterialTheme.shapes.extraSmall
            ) {
                Text(
                    when (project.status.name) {
                        "SUBMITTED" -> "Подан"
                        "DISQUALIFIED" -> "Дисквал."
                        "UNDER_REVIEW" -> "Проверка"
                        "DRAFT" -> "Черновик"
                        else -> project.status.name
                    },
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun registrationStatusColor(status: RegistrationStatus) = when (status) {
    RegistrationStatus.APPROVED -> MaterialTheme.colorScheme.primaryContainer
    RegistrationStatus.REJECTED -> MaterialTheme.colorScheme.errorContainer
    RegistrationStatus.DISQUALIFIED -> MaterialTheme.colorScheme.errorContainer
    RegistrationStatus.PENDING -> MaterialTheme.colorScheme.secondaryContainer
    else -> MaterialTheme.colorScheme.surfaceVariant
}