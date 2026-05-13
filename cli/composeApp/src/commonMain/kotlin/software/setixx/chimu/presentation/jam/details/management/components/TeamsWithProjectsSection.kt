package software.setixx.chimu.presentation.jam.details.management.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import software.setixx.chimu.api.domain.RegistrationStatus
import software.setixx.chimu.domain.model.Project
import software.setixx.chimu.domain.model.Registration
import software.setixx.chimu.presentation.components.SegmentedListItemWithExpansion
import software.setixx.chimu.presentation.components.localizeStatus

@Composable
fun TeamsWithProjectsSection(
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
private fun registrationStatusColor(status: RegistrationStatus) = when (status) {
    RegistrationStatus.APPROVED -> MaterialTheme.colorScheme.primaryContainer
    RegistrationStatus.REJECTED -> MaterialTheme.colorScheme.errorContainer
    RegistrationStatus.DISQUALIFIED -> MaterialTheme.colorScheme.errorContainer
    RegistrationStatus.PENDING -> MaterialTheme.colorScheme.secondaryContainer
    else -> MaterialTheme.colorScheme.surfaceVariant
}