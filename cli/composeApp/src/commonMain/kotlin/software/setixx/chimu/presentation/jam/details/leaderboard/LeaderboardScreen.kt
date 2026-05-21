package software.setixx.chimu.presentation.jam.details.leaderboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import software.setixx.chimu.api.domain.GameJamStatus
import software.setixx.chimu.domain.model.CriteriaScoreDetail
import software.setixx.chimu.domain.model.GameJamDetails
import software.setixx.chimu.domain.model.JamStatistics
import software.setixx.chimu.domain.model.Leaderboard
import software.setixx.chimu.domain.model.ProjectRanking

@Composable
fun LeaderboardScreen(
    jamId: String,
    jam: GameJamDetails,
    isAdminOrOrganizer: Boolean,
    viewModel: LeaderboardViewModel = koinViewModel(),
    paddingValues: PaddingValues
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    LaunchedEffect(jamId) {
        viewModel.load(jamId, loadStatistics = isAdminOrOrganizer)
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (state.isLoading) {
            LoadingIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (jam.status != GameJamStatus.COMPLETED){
            Text(
                modifier = Modifier
                    .align(Alignment.Center),
                text = "Джем еще не окончен. Дождитесь окончания"
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp)
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CompletionBanner()

                if (isAdminOrOrganizer) {
                    state.statistics?.let { stats ->
                        AdminStatisticsSection(statistics = stats)
                    }
                }

                state.leaderboard?.let { lb ->
                    LeaderboardSection(leaderboard = lb)
                } ?: run {
                    if (!state.isLoading) {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Таблица лидеров ещё не сформирована",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
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
    }
}

@Composable
private fun CompletionBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(
                    "Джем завершён!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    "Оценивание окончено — ниже итоговые результаты.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun LeaderboardSection(leaderboard: Leaderboard) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.FormatListNumbered,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Таблица лидеров",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${leaderboard.qualifiedProjects} / ${leaderboard.totalProjects} проектов",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${leaderboard.totalJudges} судей",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (leaderboard.rankings.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Нет оценённых проектов",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            leaderboard.rankings.forEach { ranking ->
                RankingCard(ranking = ranking)
            }
        }
    }
}

@Composable
private fun RankingCard(ranking: ProjectRanking) {
    var expanded by remember { mutableStateOf(false) }

    val medalColor = when (ranking.rank) {
        1 -> MaterialTheme.colorScheme.tertiary
        2 -> MaterialTheme.colorScheme.secondary
        3 -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val containerColor = when (ranking.rank) {
        1 -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f)
        2 -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
        3 -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (ranking.rank <= 3) {
                        Icon(
                            when (ranking.rank) {
                                1 -> Icons.Default.EmojiEvents
                                2 -> Icons.Default.WorkspacePremium
                                else -> Icons.Default.MilitaryTech
                            },
                            contentDescription = null,
                            tint = medalColor,
                            modifier = Modifier.size(32.dp)
                        )
                    } else {
                        Text(
                            "#${ranking.rank}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        ranking.project.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    ranking.project.teamName?.let { team ->
                        Text(
                            team,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val icon = if (ranking.score.allCriteriaRated)
                            Icons.Default.CheckCircle else Icons.Default.Pending
                        val tint = if (ranking.score.allCriteriaRated)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.outline
                        Icon(icon, null, modifier = Modifier.size(12.dp), tint = tint)
                        Text(
                            "${ranking.score.judgesRated}/${ranking.score.totalJudges} судей",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        ranking.score.total,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = medalColor.takeIf { ranking.rank <= 3 }
                            ?: MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "баллов",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider()

                    ranking.project.description?.let { desc ->
                        Text(
                            desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    ranking.project.gameUrl?.let { url ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Link, null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                url,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (ranking.score.breakdown.isNotEmpty()) {
                        Text(
                            "Оценки по критериям",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        ranking.score.breakdown.forEach { detail ->
                            CriteriaBreakdownRow(detail = detail)
                        }
                    }

                    ranking.project.submittedAt?.let { date ->
                        Text(
                            "Подан: $date",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CriteriaBreakdownRow(detail: CriteriaScoreDetail) {
    val avg = detail.averageScore.toFloatOrNull() ?: 0f
    val max = detail.maxScore.toFloat()
    val progress = if (max > 0) avg / max else 0f

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    detail.criteriaName,
                    style = MaterialTheme.typography.bodySmall
                )
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.extraSmall
                ) {
                    Text(
                        "×${detail.weight}",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${detail.averageScore} / ${detail.maxScore}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "взвеш.: ${detail.weightedScore}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(4.dp)
        )
        if (detail.scores.isNotEmpty()) {
            Text(
                "Оценки судей: ${detail.scores.joinToString(", ")}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AdminStatisticsSection(statistics: JamStatistics) {
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatBadge("Проектов", statistics.totalProjects.toString())
                StatBadge("Подано", statistics.submittedProjects.toString())
                StatBadge("Дисквал.", statistics.disqualifiedProjects.toString(),
                    isWarning = statistics.disqualifiedProjects > 0)
                StatBadge("Судей", statistics.totalJudges.toString())
            }

            if (statistics.averageScoresPerCriteria.isNotEmpty()) {
                HorizontalDivider()
                Text(
                    "Средние оценки по критериям",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                statistics.averageScoresPerCriteria.forEach { crit ->
                    val avg = crit.averageScore.toFloatOrNull() ?: 0f
                    val progress = if (crit.maxScore > 0) avg / crit.maxScore else 0f
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(crit.criteriaName, style = MaterialTheme.typography.bodySmall)
                            Text(
                                "${crit.averageScore} / ${crit.maxScore}",
                                style = MaterialTheme.typography.labelMedium,
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

            if (statistics.judgeCompletionRate.isNotEmpty()) {
                HorizontalDivider()
                Text(
                    "Завершённость судей",
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
                            Text(
                                judge.judgeNickname,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                "${judge.ratedProjects}/${judge.totalProjects} проектов",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            color = when {
                                judge.completionPercentage == 100 -> MaterialTheme.colorScheme.primaryContainer
                                judge.completionPercentage > 50  -> MaterialTheme.colorScheme.secondaryContainer
                                else                              -> MaterialTheme.colorScheme.errorContainer
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
private fun StatBadge(label: String, value: String, isWarning: Boolean = false) {
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
                fontWeight = FontWeight.Bold
            )
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}