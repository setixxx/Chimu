package software.setixx.chimu.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AppRegistration
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
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
import software.setixx.chimu.api.domain.RegistrationStatus
import software.setixx.chimu.api.domain.RegistrationStatus.*
import software.setixx.chimu.domain.model.GameJamDetails
import software.setixx.chimu.domain.model.Registration
import software.setixx.chimu.presentation.jam.details.components.ListWithTwoIcons
import software.setixx.chimu.presentation.main.components.JamBanner
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
        Card(modifier = Modifier.fillMaxWidth()) {
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

        Card(modifier = Modifier.fillMaxWidth()) {
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

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large),
        ) {
            ListWithTwoIcons(
                contentDescription = "Критерии оценивания",
                header = "Критерии оценивания",
                onClick = { isRulesExpanded = !isRulesExpanded },
                trailingIcon = if (isRulesExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore
            )
        }
        AnimatedVisibility(
            visible = isRulesExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.largeIncreased),
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
            ) {
                jam.criteria.forEachIndexed { index, criteria ->
                    val colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.onSecondary)
                    SegmentedListItem(
                        colors = colors,
                        selected = false,
                        onClick = {},
                        shapes = ListItemDefaults.segmentedShapes(
                            index = index,
                            count = jam.criteria.size
                        ),
                        content = {
                            Text(
                                criteria.name
                            )
                        },
                        supportingContent = {
                            Column {
                                Text(criteria.description ?: "Без описания")
                                Row {
                                    Text(
                                        "Макс: ${criteria.maxScore}"
                                    )
                                    Text(
                                        "| Вес: ${criteria.weight}"
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large),
        ) {
            ListWithTwoIcons(
                contentDescription = "Судьи",
                header = "Судьи",
                onClick = { isJudgesExpanded = !isJudgesExpanded },
                trailingIcon = if (isJudgesExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore
            )
        }
        AnimatedVisibility(
            visible = isJudgesExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.largeIncreased),
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
            ) {
                jam.judges.forEachIndexed { index, judge ->
                    val colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.onSecondary)
                    SegmentedListItem(
                        colors = colors,
                        selected = false,
                        onClick = {},
                        shapes = ListItemDefaults.segmentedShapes(
                            index = index,
                            count = jam.judges.size
                        ),
                        content = {
                            Text(
                                judge.nickname
                            )
                        },
                        supportingContent = {
                            Text("Назначен: ${DateTimeUtils.formatDateTime(judge.assignedAt)}")
                        }
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large),
        ) {
            ListWithTwoIcons(
                contentDescription = "Команды-участники",
                header = "Команды-участники",
                onClick = { isTeamsExpanded = !isTeamsExpanded },
                trailingIcon = if (isTeamsExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore
            )
        }
        AnimatedVisibility(
            visible = isTeamsExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.largeIncreased),
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
            ) {
                registrations.forEachIndexed { index, team ->
                    val colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.onSecondary)
                    SegmentedListItem(
                        colors = colors,
                        selected = false,
                        onClick = {},
                        shapes = ListItemDefaults.segmentedShapes(
                            index = index,
                            count = registrations.size
                        ),
                        content = {
                            Text(
                                team.teamName
                            )
                        },
                        supportingContent = {
                            Text("От: ${team.registeredByNickname}")
                        },
                        trailingContent = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Badge { Text(localizeStatus(team.status)) }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RegisteredTeamsSection(
    registrations: List<Registration>,
    title: String = "Команды-участники",
    actions: @Composable (Registration) -> Unit = {}
) {
    val visibleRegistrations = registrations.filter { it.status != RegistrationStatus.WITHDRAWN && it.status != RegistrationStatus.CANCELLED }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("$title (${visibleRegistrations.size})", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            if (visibleRegistrations.isEmpty()) {
                Text("Пока нет зарегистрированных команд")
            } else {
                visibleRegistrations.forEachIndexed { index, reg ->
                    ListItem(
                        headlineContent = { Text(reg.teamName) },
                        supportingContent = { Text("От: ${reg.registeredByNickname}") },
                        trailingContent = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Badge { Text(localizeStatus(reg.status)) }
                            }
                        }
                    )
                    if (index < visibleRegistrations.lastIndex) HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun StagePlaceholder(message: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

fun localizeStatus(status: RegistrationStatus): String{
    return when (status){
        CANCELLED -> "Отклонена"
        WITHDRAWN -> "Покинула"
        APPROVED -> "Одобрена"
        REJECTED -> "Отклонена"
        PENDING -> "Рассматривается"
        DISQUALIFIED -> "Дисковалицирована"
    }
}
