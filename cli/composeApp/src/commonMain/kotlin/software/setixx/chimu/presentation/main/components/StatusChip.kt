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
        GameJamStatus.DRAFT -> "Черновик" to MaterialTheme.colorScheme.surfaceVariant
        GameJamStatus.ANNOUNCED -> "Аннонсирован" to MaterialTheme.colorScheme.primaryContainer
        GameJamStatus.REGISTRATION_OPEN -> "Регистрация" to MaterialTheme.colorScheme.onPrimaryFixedVariant
        GameJamStatus.REGISTRATION_CLOSED -> "Подготовка" to MaterialTheme.colorScheme.onPrimaryFixedVariant
        GameJamStatus.IN_PROGRESS -> "В процессе" to MaterialTheme.colorScheme.primaryContainer
        GameJamStatus.JUDGING -> "Оценивание" to MaterialTheme.colorScheme.onTertiaryFixedVariant
        GameJamStatus.COMPLETED -> "Завершен" to MaterialTheme.colorScheme.tertiaryContainer
        GameJamStatus.CANCELLED -> "Отменен" to MaterialTheme.colorScheme.errorContainer
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