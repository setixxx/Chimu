package software.setixx.chimu.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import software.setixx.chimu.domain.model.ProjectStatus

@Composable
fun ProjectStatusChip(status: ProjectStatus) {
    val (text, color) = when (status) {
        ProjectStatus.DRAFT -> "Черновик" to MaterialTheme.colorScheme.surfaceVariant
        ProjectStatus.SUBMITTED -> "Отправлен" to MaterialTheme.colorScheme.primary
        ProjectStatus.PUBLISHED -> "Опубликован" to MaterialTheme.colorScheme.tertiary
        ProjectStatus.DISQUALIFIED -> "Дисквалифицирован" to MaterialTheme.colorScheme.error
        else -> status.name to MaterialTheme.colorScheme.surfaceVariant
    }

    AssistChip(
        onClick = { },
        label = { Text(text, style = MaterialTheme.typography.labelSmall) },
        leadingIcon = {
            Icon(
                Icons.Default.Circle,
                null,
                modifier = Modifier.size(6.dp),
                tint = color
            )
        }
    )
}