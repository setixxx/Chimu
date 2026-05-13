package software.setixx.chimu.presentation.jam.details.management.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import software.setixx.chimu.domain.model.JamStatistics

@Composable
fun JamStatisticsCard(statistics: JamStatistics) {
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
                StatChip(label = "Проектов", value = statistics.totalProjects.toString())
                StatChip(label = "Подано", value = statistics.submittedProjects.toString())
                StatChip(
                    label = "Дисквалиф.",
                    value = statistics.disqualifiedProjects.toString(),
                    isWarning = statistics.disqualifiedProjects > 0
                )
                StatChip(label = "Судей", value = statistics.totalJudges.toString())
            }

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