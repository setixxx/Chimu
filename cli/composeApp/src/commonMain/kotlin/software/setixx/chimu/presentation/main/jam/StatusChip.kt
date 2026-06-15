package software.setixx.chimu.presentation.main.jam

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import software.setixx.chimu.api.domain.GameJamStatus

@Composable
fun StatusChip(status: GameJamStatus) {
    val (text, color) = when (status) {
        GameJamStatus.DRAFT -> "Черновик" to MaterialTheme.colorScheme.surface
        GameJamStatus.ANNOUNCED -> "Аннонсирован" to MaterialTheme.colorScheme.primary
        GameJamStatus.REGISTRATION_OPEN -> "Регистрация" to MaterialTheme.colorScheme.secondary
        GameJamStatus.REGISTRATION_CLOSED -> "Подготовка" to MaterialTheme.colorScheme.secondary
        GameJamStatus.IN_PROGRESS -> "В процессе" to MaterialTheme.colorScheme.inversePrimary
        GameJamStatus.JUDGING -> "Оценивание" to MaterialTheme.colorScheme.tertiary
        GameJamStatus.COMPLETED -> "Завершен" to MaterialTheme.colorScheme.surfaceContainerHighest
        GameJamStatus.CANCELLED -> "Отменен" to MaterialTheme.colorScheme.error
    }

    Surface(
        color = color,
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 6.dp, horizontal = 12.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}