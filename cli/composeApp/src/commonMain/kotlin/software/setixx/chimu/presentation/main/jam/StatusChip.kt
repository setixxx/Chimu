package software.setixx.chimu.presentation.main.jam

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import chimu.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import software.setixx.chimu.api.domain.GameJamStatus

@Composable
fun StatusChip(status: GameJamStatus) {
    val (text, color) = when (status) {
        GameJamStatus.DRAFT -> stringResource(Res.string.status_draft) to MaterialTheme.colorScheme.surface
        GameJamStatus.ANNOUNCED -> stringResource(Res.string.status_announced) to MaterialTheme.colorScheme.primary
        GameJamStatus.REGISTRATION_OPEN -> stringResource(Res.string.status_registration_open) to MaterialTheme.colorScheme.secondary
        GameJamStatus.REGISTRATION_CLOSED -> stringResource(Res.string.status_registration_closed) to MaterialTheme.colorScheme.secondary
        GameJamStatus.IN_PROGRESS -> stringResource(Res.string.status_in_progress) to MaterialTheme.colorScheme.inversePrimary
        GameJamStatus.JUDGING -> stringResource(Res.string.status_judging) to MaterialTheme.colorScheme.tertiary
        GameJamStatus.COMPLETED -> stringResource(Res.string.status_completed) to MaterialTheme.colorScheme.surfaceContainerHighest
        GameJamStatus.CANCELLED -> stringResource(Res.string.status_cancelled) to MaterialTheme.colorScheme.error
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