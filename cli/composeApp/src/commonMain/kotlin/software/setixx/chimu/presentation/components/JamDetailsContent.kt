package software.setixx.chimu.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AppRegistration
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import software.setixx.chimu.domain.model.GameJamDetails
import software.setixx.chimu.domain.model.Registration

@Composable
fun JamOverviewSection(
    jam: GameJamDetails,
    showRules: Boolean = true,
    showCriteria: Boolean = true,
    showJudges: Boolean = true
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Информация", style = MaterialTheme.typography.titleLarge)
            Text(jam.description ?: "Нет описания")
            jam.theme?.let { Text("Тема: $it", style = MaterialTheme.typography.bodyLarge) }
            if (showRules && !jam.rules.isNullOrBlank()) {
                HorizontalDivider()
                Text("Правила", style = MaterialTheme.typography.titleMedium)
                Text(jam.rules)
            }
        }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Даты", style = MaterialTheme.typography.titleMedium)
            InfoRow(
                icon = Icons.Default.AppRegistration,
                label = "Регистрация",
                value = "${jam.registrationStart} - ${jam.registrationEnd}"
            )
            InfoRow(
                icon = Icons.Default.PlayArrow,
                label = "Джем",
                value = "${jam.jamStart} - ${jam.jamEnd}"
            )
            InfoRow(
                icon = Icons.Default.Star,
                label = "Оценивание",
                value = "${jam.judgingStart} - ${jam.judgingEnd}"
            )
        }
    }

    if (showCriteria) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Критерии оценивания (${jam.criteria.size})", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                if (jam.criteria.isEmpty()) {
                    Text("Критерии не добавлены.")
                } else {
                    jam.criteria.sortedBy { it.orderIndex }.forEachIndexed { index, criteria ->
                        ListItem(
                            headlineContent = { Text(criteria.name) },
                            supportingContent = {
                                Text("${criteria.description ?: "Без описания"}\nМакс: ${criteria.maxScore} | Вес: ${criteria.weight}")
                            }
                        )
                        if (index < jam.criteria.lastIndex) HorizontalDivider()
                    }
                }
            }
        }
    }

    if (showJudges) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Судьи (${jam.judges.size})", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                if (jam.judges.isEmpty()) {
                    Text("Судьи не назначены.")
                } else {
                    jam.judges.forEachIndexed { index, judge ->
                        ListItem(
                            headlineContent = { Text(judge.nickname) },
                            supportingContent = { Text("Назначен: ${judge.assignedAt}") }
                        )
                        if (index < jam.judges.lastIndex) HorizontalDivider()
                    }
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
    val visibleRegistrations = registrations.filter { it.status != "WITHDRAWN" && it.status != "CANCELLED" }

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
                                Badge { Text(reg.status) }
                                actions(reg)
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
