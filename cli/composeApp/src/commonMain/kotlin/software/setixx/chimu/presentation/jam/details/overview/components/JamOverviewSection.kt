package software.setixx.chimu.presentation.jam.details.overview.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Rule
import androidx.compose.material.icons.filled.AppRegistration
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import chimu.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import software.setixx.chimu.domain.model.GameJamDetails
import software.setixx.chimu.presentation.components.InfoRow
import software.setixx.chimu.presentation.utils.DateTimeUtils

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun JamOverviewSection(
    jam: GameJamDetails,
    userId: String,
    onNavigateToAlienProfile: (String) -> Unit,
    onNavigateToOwnProfile: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.largeIncreased
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Icon(
                        Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(Res.string.description), style = MaterialTheme.typography.titleLarge)
                }
                Text(jam.description)
                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Rule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(Res.string.rules), style = MaterialTheme.typography.titleLarge)
                }
                Text(jam.rules)
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.largeIncreased
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(stringResource(Res.string.dates), style = MaterialTheme.typography.titleMedium)
                InfoRow(
                    icon = Icons.Default.AppRegistration,
                    label = stringResource(Res.string.registration),
                    value = stringResource(
                        Res.string.date_range_format,
                        DateTimeUtils.formatDateTime(jam.registrationStart),
                        DateTimeUtils.formatDateTime(jam.registrationEnd)
                    )
                )
                InfoRow(
                    icon = Icons.Default.PlayArrow,
                    label = stringResource(Res.string.jam),
                    value = stringResource(
                        Res.string.date_range_format,
                        DateTimeUtils.formatDateTime(jam.jamStart),
                        DateTimeUtils.formatDateTime(jam.jamEnd)
                    ),
                )
                InfoRow(
                    icon = Icons.Default.Star,
                    label = stringResource(Res.string.judging),
                    value = stringResource(
                        Res.string.date_range_format,
                        DateTimeUtils.formatDateTime(jam.judgingStart),
                        DateTimeUtils.formatDateTime(jam.judgingEnd)
                    )
                )
            }
        }

        ManagementListCard(
            title = stringResource(Res.string.jam_details_judges_title),
            titleIcon = Icons.Default.Gavel,
            items = jam.judges,
            emptyText = stringResource(Res.string.jam_details_judges_empty),
            onButtonClick = { },
            onItemClick = { judge ->
                if (judge.userId != userId){
                    onNavigateToAlienProfile(judge.userId)
                } else {
                    onNavigateToOwnProfile()
                }
            },
            itemHeadline = { judge -> Text(judge.nickname) },
            itemSupportingContent = { judge ->
                Text(stringResource(Res.string.jam_details_judge_assigned_at, DateTimeUtils.formatDateTime(judge.assignedAt)))
            }
        )

        ManagementListCard(
            title = stringResource(Res.string.jam_details_criteria_title),
            titleIcon = Icons.AutoMirrored.Filled.Rule,
            items = jam.criteria.sortedBy { it.orderIndex },
            emptyText = stringResource(Res.string.jam_details_criteria_empty),
            onButtonClick = {  },
            itemHeadline = { criteria -> Text(criteria.name) },
            itemSupportingContent = { criteria ->
                Text(
                    stringResource(
                        Res.string.max_score_and_weight_format,
                        stringResource(Res.string.judging_max_score, criteria.maxScore),
                        stringResource(Res.string.judging_weight, criteria.weight.toString())
                    )
                )
            }
        )
    }
}