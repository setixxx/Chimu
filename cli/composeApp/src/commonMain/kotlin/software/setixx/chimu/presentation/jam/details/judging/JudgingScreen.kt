package software.setixx.chimu.presentation.jam.details.judging

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import software.setixx.chimu.api.domain.UserRole
import software.setixx.chimu.data.remote.dto.RatingCriteriaResponse
import software.setixx.chimu.domain.model.GameJamDetails
import software.setixx.chimu.domain.model.MyRating
import software.setixx.chimu.domain.model.Project

@Composable
fun JudgingScreen(
    jamId: String,
    jam: GameJamDetails,
    userRole: UserRole?,
    viewModel: JudgingViewModel = koinViewModel(),
    paddingValues: PaddingValues
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(jamId, userRole) {
        when (userRole) {
            UserRole.JUDGE -> viewModel.loadAsJudge(jamId)
            UserRole.PARTICIPANT -> viewModel.loadAsParticipant(jamId)
            else -> viewModel.loadAsJudge(jamId)
        }
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
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LoadingIndicator()
            }
        } else {
            when (userRole) {
                UserRole.JUDGE -> JudgeView(
                    state = state,
                    jamCriteria = jam.criteria,
                    onSelectProject = { viewModel.selectProject(it) },
                    onBack = { viewModel.clearSelectedProject() },
                    onRate = { projectId, criteriaId, score, comment ->
                        viewModel.rateProject(projectId, criteriaId, score, comment)
                    },
                    onUpdate = { ratingId, projectId, score, comment ->
                        viewModel.updateRating(ratingId, projectId, score, comment)
                    },
                    onDelete = { ratingId, projectId ->
                        viewModel.deleteRating(ratingId, projectId)
                    }
                )
                UserRole.PARTICIPANT -> ParticipantJudgingView(
                    userProject = state.userProject
                )
                else -> JudgeView(
                    state = state,
                    jamCriteria = jam.criteria,
                    onSelectProject = { viewModel.selectProject(it) },
                    onBack = { viewModel.clearSelectedProject() },
                    onRate = { projectId, criteriaId, score, comment ->
                        viewModel.rateProject(projectId, criteriaId, score, comment)
                    },
                    onUpdate = { ratingId, projectId, score, comment ->
                        viewModel.updateRating(ratingId, projectId, score, comment)
                    },
                    onDelete = { ratingId, projectId ->
                        viewModel.deleteRating(ratingId, projectId)
                    }
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun JudgeView(
    state: JudgingState,
    jamCriteria: List<RatingCriteriaResponse>,
    onSelectProject: (Project) -> Unit,
    onBack: () -> Unit,
    onRate: (projectId: String, criteriaId: String, score: Int, comment: String?) -> Unit,
    onUpdate: (ratingId: String, projectId: String, score: Int, comment: String?) -> Unit,
    onDelete: (ratingId: String, projectId: String) -> Unit
) {
    val scrollState = rememberScrollState()

    val selectedProject = state.selectedProject
    if (selectedProject != null) {
        ProjectRatingPanel(
            project = selectedProject,
            criteria = jamCriteria,
            myRatings = state.myRatings,
            isRatingLoading = state.isRatingLoading,
            isActionLoading = state.isActionLoading,
            onBack = onBack,
            onRate = onRate,
            onUpdate = onUpdate,
            onDelete = onDelete
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            state.judgeProgress?.let { progress ->
                JudgeProgressCard(
                    ratedProjects = progress.ratedProjects,
                    totalProjects = progress.totalProjects
                )
            }

            state.judgeProgress?.let { progress ->
                if (progress.missingProjects.isNotEmpty()) {
                    MissingRatingsCard(missingCount = progress.missingProjects.size)
                }
            }

            Text(
                text = "Проекты для оценки (${state.projects.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (state.projects.isEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Inbox,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Нет проектов для оценки",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                val ratedProjectIds = state.judgeProgress?.missingProjects
                    ?.map { it.projectId }?.toSet() ?: emptySet()

                state.projects.forEach { project ->
                    val isMissingRating = ratedProjectIds.contains(project.id)
                    ProjectCard(
                        project = project,
                        hasUnratedCriteria = isMissingRating,
                        onClick = { onSelectProject(project) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun JudgeProgressCard(ratedProjects: Int, totalProjects: Int) {
    val progress = if (totalProjects > 0) ratedProjects.toFloat() / totalProjects else 0f
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Мой прогресс",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    "$ratedProjects / $totalProjects",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                if (ratedProjects == totalProjects && totalProjects > 0)
                    "Все проекты оценены ✓"
                else
                    "Осталось оценить: ${totalProjects - ratedProjects}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun MissingRatingsCard(missingCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp)
            )
            Text(
                "$missingCount ${if (missingCount == 1) "проект требует" else "проекта требуют"} ваших оценок",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun ProjectCard(
    project: Project,
    hasUnratedCriteria: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (hasUnratedCriteria)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    project.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                project.teamName?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Команда: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (hasUnratedCriteria) {
                Badge(containerColor = MaterialTheme.colorScheme.error) {
                    Text("!", color = MaterialTheme.colorScheme.onError)
                }
                Spacer(modifier = Modifier.width(8.dp))
            } else {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Открыть",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProjectRatingPanel(
    project: Project,
    criteria: List<RatingCriteriaResponse>,
    myRatings: List<MyRating>,
    isRatingLoading: Boolean,
    isActionLoading: Boolean,
    onBack: () -> Unit,
    onRate: (projectId: String, criteriaId: String, score: Int, comment: String?) -> Unit,
    onUpdate: (ratingId: String, projectId: String, score: Int, comment: String?) -> Unit,
    onDelete: (ratingId: String, projectId: String) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 3.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                }
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text(
                        project.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    project.teamName?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProjectInfoCard(project = project)

            if (isRatingLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            } else {
                Text(
                    "Оценки по критериям",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (criteria.isEmpty()) {
                    Text(
                        "Критерии оценивания не заданы",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    criteria.sortedBy { it.orderIndex }.forEach { criterion ->
                        val existingRating = myRatings.find { it.criteriaId == criterion.id }
                        CriteriaRatingCard(
                            criterion = criterion,
                            existingRating = existingRating,
                            isActionLoading = isActionLoading,
                            onRate = { score, comment ->
                                onRate(project.id, criterion.id, score, comment)
                            },
                            onUpdate = { score, comment ->
                                existingRating?.let {
                                    onUpdate(it.id, project.id, score, comment)
                                }
                            },
                            onDelete = {
                                existingRating?.let {
                                    onDelete(it.id, project.id)
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun ProjectInfoCard(project: Project) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            project.description?.let { desc ->
                Text(desc, style = MaterialTheme.typography.bodyMedium)
                HorizontalDivider()
            }
            project.gameUrl?.let { url ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Link,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        url,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            project.submittedAt?.let { date ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Подан: $date",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun CriteriaRatingCard(
    criterion: RatingCriteriaResponse,
    existingRating: MyRating?,
    isActionLoading: Boolean,
    onRate: (score: Int, comment: String?) -> Unit,
    onUpdate: (score: Int, comment: String?) -> Unit,
    onDelete: () -> Unit
) {
    var isEditing by remember(existingRating) { mutableStateOf(existingRating == null) }
    var sliderValue by remember(existingRating, criterion) {
        mutableFloatStateOf(
            existingRating?.score?.toFloatOrNull() ?: 0f
        )
    }
    var comment by remember(existingRating) {
        mutableStateOf(existingRating?.comment ?: "")
    }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (existingRating != null)
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        criterion.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    criterion.description?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Макс: ${criterion.maxScore}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Вес: ${criterion.weight}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider()

            if (existingRating != null && !isEditing) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "${existingRating.score} / ${criterion.maxScore}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Row {
                        IconButton(
                            onClick = {
                                sliderValue = existingRating.score.toFloatOrNull() ?: 0f
                                comment = existingRating.comment ?: ""
                                isEditing = true
                            }
                        ) {
                            Icon(Icons.Default.Edit, "Изменить")
                        }
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(
                                Icons.Default.Delete,
                                "Удалить",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                existingRating.comment?.let { c ->
                    Text(
                        "Комментарий: $c",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Оценка", style = MaterialTheme.typography.labelMedium)
                        Text(
                            "${sliderValue.toInt()} / ${criterion.maxScore}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = sliderValue,
                        onValueChange = { sliderValue = it },
                        valueRange = 0f..criterion.maxScore.toFloat(),
                        steps = criterion.maxScore - 1,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = { Text("Комментарий (необязательно)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (existingRating != null) {
                            OutlinedButton(
                                onClick = { isEditing = false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Отмена")
                            }
                        }
                        Button(
                            onClick = {
                                val score = sliderValue.toInt()
                                if (existingRating != null) {
                                    onUpdate(score, comment)
                                } else {
                                    onRate(score, comment)
                                }
                                isEditing = false
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isActionLoading
                        ) {
                            if (isActionLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text(if (existingRating != null) "Обновить" else "Сохранить")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Удалить оценку?") },
            text = { Text("Оценка по критерию «${criterion.name}» будет удалена.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
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


@Composable
private fun ParticipantJudgingView(userProject: Project?) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Info banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.HourglassTop,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(24.dp).padding(top = 2.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Этап оценивания",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        "Сейчас проходит этап оценивания. Судьи изучают и оценивают работы участников. Дождитесь завершения этапа — результаты будут объявлены после его окончания.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }

        Text(
            "Ваш проект",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        if (userProject == null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.FolderOff,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
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
        } else {
            ParticipantProjectCard(project = userProject)
        }
    }
}

@Composable
private fun ParticipantProjectCard(project: Project) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    project.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                ProjectStatusBadge(status = project.status.name)
            }

            project.teamName?.let { team ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Group,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        team,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            project.description?.let { desc ->
                HorizontalDivider()
                Text(
                    desc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            project.gameUrl?.let { url ->
                HorizontalDivider()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Link,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        url,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            project.submittedAt?.let { date ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CheckCircleOutline,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Подан: $date",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.small
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Проект заблокирован на время оценивания",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ProjectStatusBadge(status: String) {
    val (color, text) = when (status.uppercase()) {
        "SUBMITTED" -> MaterialTheme.colorScheme.primaryContainer to "Подан"
        "UNDER_REVIEW" -> MaterialTheme.colorScheme.tertiaryContainer to "На проверке"
        "DISQUALIFIED" -> MaterialTheme.colorScheme.errorContainer to "Дисквалифицирован"
        "DRAFT" -> MaterialTheme.colorScheme.surfaceVariant to "Черновик"
        else -> MaterialTheme.colorScheme.surfaceVariant to status
    }
    Surface(
        color = color,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}