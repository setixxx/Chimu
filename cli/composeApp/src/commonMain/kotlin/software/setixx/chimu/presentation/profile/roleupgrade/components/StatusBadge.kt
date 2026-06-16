package software.setixx.chimu.presentation.profile.roleupgrade.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import chimu.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import software.setixx.chimu.api.domain.RoleRequestStatus

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun StatusBadge(status: RoleRequestStatus) {
    val (containerColor, contentColor, labelRes) = when (status) {
        RoleRequestStatus.PENDING -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            Res.string.role_upgrade_filter_pending
        )
        RoleRequestStatus.APPROVED -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            Res.string.registration_status_approved
        )
        RoleRequestStatus.REJECTED -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            Res.string.registration_status_rejected
        )
        RoleRequestStatus.CANCELLED -> Triple(
            MaterialTheme.colorScheme.surfaceContainerHighest,
            MaterialTheme.colorScheme.onSurfaceVariant,
            Res.string.role_upgrade_cancel
        )
    }

    Surface(
        shape = MaterialTheme.shapes.largeIncreased,
        color = containerColor
    ) {
        Text(
            text = stringResource(labelRes),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            color = contentColor
        )
    }
}