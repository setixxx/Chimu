package software.setixx.chimu.presentation.jam.details.judging.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProjectStatusBadge(status: String) {
    val (color, text) = when (status.uppercase()) {
        "SUBMITTED" -> MaterialTheme.colorScheme.primaryContainer to "Подан"
        "UNDER_REVIEW" -> MaterialTheme.colorScheme.tertiaryContainer to "На проверке"
        "DISQUALIFIED" -> MaterialTheme.colorScheme.errorContainer to "Дисквалифицирован"
        "DRAFT" -> MaterialTheme.colorScheme.surfaceVariant to "Черновик"
        else -> MaterialTheme.colorScheme.surfaceVariant to status
    }
    Surface(
        color = color,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}