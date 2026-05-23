package software.setixx.chimu.presentation.jam.details.overview.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Rule
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AppRegistration
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import software.setixx.chimu.domain.model.GameJamDetails
import software.setixx.chimu.domain.model.Registration
import software.setixx.chimu.presentation.components.InfoRow
import software.setixx.chimu.presentation.components.localizeStatus
import software.setixx.chimu.presentation.jam.details.components.ListWithTwoIcons
import software.setixx.chimu.presentation.jam.details.management.components.ManagementListCard
import software.setixx.chimu.presentation.utils.DateTimeUtils

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun JamOverviewSection(
    jam: GameJamDetails,
    registrations: List<Registration>,
) {
    var isRulesExpanded by remember { mutableStateOf(true) }
    var isJudgesExpanded by remember { mutableStateOf(true) }
    var isTeamsExpanded by remember { mutableStateOf(false) }

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
                Text("Описание", style = MaterialTheme.typography.titleLarge)
                Text(jam.description)
                HorizontalDivider()
                Text("Правила", style = MaterialTheme.typography.titleMedium)
                Text(jam.rules)
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.largeIncreased
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Даты", style = MaterialTheme.typography.titleMedium)
                InfoRow(
                    icon = Icons.Default.AppRegistration,
                    label = "Регистрация",
                    value = "${DateTimeUtils.formatDateTime(jam.registrationStart)} - ${DateTimeUtils.formatDateTime(jam.registrationEnd)}"
                )
                InfoRow(
                    icon = Icons.Default.PlayArrow,
                    label = "Джем",
                    value = "${DateTimeUtils.formatDateTime(jam.jamStart)} - ${DateTimeUtils.formatDateTime(jam.jamEnd)}"
                )
                InfoRow(
                    icon = Icons.Default.Star,
                    label = "Оценивание",
                    value = "${DateTimeUtils.formatDateTime(jam.judgingStart)} - ${DateTimeUtils.formatDateTime(jam.judgingEnd)}"
                )
            }
        }

        ManagementListCard(
            title = "Судьи",
            titleIcon = Icons.Default.Gavel,
            items = jam.judges,
            emptyText = "Судьи не назначены.",
            onButtonClick = { },
            itemHeadline = { judge -> Text(judge.nickname) },
            itemSupportingContent = { judge ->
                Text("Назначен: ${DateTimeUtils.formatDateTime(judge.assignedAt)}")
            }
        )

        ManagementListCard(
            title = "Критерии оценивания",
            titleIcon = Icons.AutoMirrored.Filled.Rule,
            items = jam.criteria.sortedBy { it.orderIndex },
            emptyText = "Критерии не добавлены.",
            onButtonClick = {  },
            itemHeadline = { criteria -> Text(criteria.name) },
            itemSupportingContent = { criteria ->
                Text("Макс: ${criteria.maxScore} • Вес: ${criteria.weight}")
            }
        )
    }
}