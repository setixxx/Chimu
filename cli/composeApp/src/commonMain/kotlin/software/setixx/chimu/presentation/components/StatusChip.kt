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
import software.setixx.chimu.domain.model.GameJamStatus

@Composable
fun StatusChip(status: GameJamStatus) {
    val (text, color) = when (status) {
        GameJamStatus.REGISTRATION_OPEN -> "Регистрация" to MaterialTheme.colorScheme.tertiary
        GameJamStatus.IN_PROGRESS -> "В процессе" to MaterialTheme.colorScheme.primary
        GameJamStatus.JUDGING -> "Оценивание" to MaterialTheme.colorScheme.secondary
        GameJamStatus.COMPLETED -> "Завершен" to MaterialTheme.colorScheme.surfaceVariant
        GameJamStatus.CANCELLED -> "Отменен" to MaterialTheme.colorScheme.error
        else -> status.name to MaterialTheme.colorScheme.surfaceVariant
    }

    AssistChip(
        onClick = { },
        label = { Text(text) },
        leadingIcon = {
            Icon(
                Icons.Default.Circle,
                null,
                modifier = Modifier.size(8.dp),
                tint = color
            )
        }
    )
}