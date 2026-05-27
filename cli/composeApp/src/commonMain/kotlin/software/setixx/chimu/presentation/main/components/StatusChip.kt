package software.setixx.chimu.presentation.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import software.setixx.chimu.api.domain.GameJamStatus

@Composable
fun StatusChip(status: GameJamStatus) {
    val (text, color) = when (status) {
        GameJamStatus.DRAFT -> "Черновик" to MaterialTheme.colorScheme.tertiary
        GameJamStatus.ANNOUNCED -> "Аннонсирован" to MaterialTheme.colorScheme.primary
        GameJamStatus.REGISTRATION_OPEN -> "Регистрация" to MaterialTheme.colorScheme.tertiary
        GameJamStatus.IN_PROGRESS -> "В процессе" to MaterialTheme.colorScheme.surface
        GameJamStatus.JUDGING -> "Оценивание" to MaterialTheme.colorScheme.secondary
        GameJamStatus.COMPLETED -> "Завершен" to MaterialTheme.colorScheme.surfaceVariant
        GameJamStatus.CANCELLED -> "Отменен" to MaterialTheme.colorScheme.error
        else -> status.name to MaterialTheme.colorScheme.surfaceVariant
    }

    Row(
        modifier = Modifier
            .clip(MaterialTheme.shapes.large)
            .background(color)
            .padding(vertical = 6.dp, horizontal = 12.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            color = Color.White
        )
    }
}