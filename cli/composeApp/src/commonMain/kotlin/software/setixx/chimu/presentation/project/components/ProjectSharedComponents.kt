package software.setixx.chimu.presentation.project.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import software.setixx.chimu.domain.model.ProjectDetails
import software.setixx.chimu.presentation.jam.details.judging.components.ProjectStatusBadge

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProjectHeaderCard(
    project: ProjectDetails,
    canEdit: Boolean,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        project.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    project.teamName?.let {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            it,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    Text(
                        project.jamName,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ProjectStatusBadge(status = project.status)
                    if (canEdit) {
                        FilledTonalIconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, contentDescription = "Редактировать")
                        }
                    }
                }
            }

            project.description?.takeIf { it.isNotBlank() }?.let {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                )
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            project.gameUrl?.takeIf { it.isNotBlank() }?.let {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Link,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun ProjectEditDialog(
    title: String,
    projectTitle: String,
    projectDescription: String,
    projectGameUrl: String,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onGameUrlChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = projectTitle,
                    onValueChange = onTitleChange,
                    label = { Text("Название") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = projectDescription,
                    onValueChange = onDescriptionChange,
                    label = { Text("Описание") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )
                OutlinedTextField(
                    value = projectGameUrl,
                    onValueChange = onGameUrlChange,
                    label = { Text("Ссылка на игру (необязательно)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = projectTitle.isNotBlank()
            ) { Text("Сохранить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}