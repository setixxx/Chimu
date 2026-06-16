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
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import chimu.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import software.setixx.chimu.domain.model.Leaderboard

@Composable
fun LeaderboardCard(leaderboard: Leaderboard) {
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
                    stringResource(Res.string.leaderboard_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    stringResource(
                        Res.string.management_leaderboard_rated_count,
                        leaderboard.qualifiedProjects,
                        leaderboard.totalProjects
                    ),
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
                        stringResource(Res.string.rank_format, ranking.rank),
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
                    stringResource(Res.string.management_leaderboard_more_projects, leaderboard.rankings.size - 10),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}