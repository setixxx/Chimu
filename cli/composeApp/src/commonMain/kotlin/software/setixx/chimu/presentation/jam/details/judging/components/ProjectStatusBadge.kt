package software.setixx.chimu.presentation.jam.details.judging.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import chimu.composeapp.generated.resources.*
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import software.setixx.chimu.api.domain.GameJamStatus
import software.setixx.chimu.api.domain.ProjectStatus

@Composable
fun getProjectStatusString(status: ProjectStatus): String {
    return stringResource(getProjectStatusRes(status))
}

fun getProjectStatusRes(status: ProjectStatus): StringResource {
    return when (status) {
        ProjectStatus.SUBMITTED -> Res.string.status_submitted
        ProjectStatus.UNDER_REVIEW -> Res.string.status_under_review
        ProjectStatus.DISQUALIFIED -> Res.string.status_disqualified
        ProjectStatus.DRAFT -> Res.string.status_draft
    }
}

@Composable
fun getGameJamStatusString(status: GameJamStatus): String {
    return stringResource(getGameJamStatusRes(status))
}

fun getGameJamStatusRes(status: GameJamStatus): StringResource {
    return when (status) {
        GameJamStatus.DRAFT -> Res.string.status_draft
        GameJamStatus.ANNOUNCED -> Res.string.status_announced
        GameJamStatus.REGISTRATION_OPEN -> Res.string.status_registration_open
        GameJamStatus.REGISTRATION_CLOSED -> Res.string.status_registration_closed
        GameJamStatus.IN_PROGRESS -> Res.string.status_in_progress
        GameJamStatus.JUDGING -> Res.string.status_judging
        GameJamStatus.COMPLETED -> Res.string.status_completed
        GameJamStatus.CANCELLED -> Res.string.status_cancelled
    }
}

@Composable
fun ProjectStatusBadge(status: ProjectStatus) {
    val (color, textRes) = when (status) {
        ProjectStatus.SUBMITTED -> MaterialTheme.colorScheme.primaryContainer to Res.string.status_submitted
        ProjectStatus.UNDER_REVIEW -> MaterialTheme.colorScheme.tertiaryContainer to Res.string.status_under_review
        ProjectStatus.DISQUALIFIED -> MaterialTheme.colorScheme.errorContainer to Res.string.status_disqualified
        ProjectStatus.DRAFT -> MaterialTheme.colorScheme.surfaceVariant to Res.string.status_draft
    }
    Surface(
        color = color,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            stringResource(textRes),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}