package software.setixx.chimu.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import software.setixx.chimu.api.domain.RegistrationStatus
import software.setixx.chimu.api.domain.RegistrationStatus.*
import software.setixx.chimu.domain.model.Registration

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
