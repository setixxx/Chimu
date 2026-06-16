package software.setixx.chimu.presentation.jam.details.management.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import chimu.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import software.setixx.chimu.api.domain.RegistrationStatus
import software.setixx.chimu.api.domain.RegistrationStatus.APPROVED
import software.setixx.chimu.api.domain.RegistrationStatus.CANCELLED
import software.setixx.chimu.api.domain.RegistrationStatus.DISQUALIFIED
import software.setixx.chimu.api.domain.RegistrationStatus.PENDING
import software.setixx.chimu.api.domain.RegistrationStatus.REJECTED
import software.setixx.chimu.api.domain.RegistrationStatus.WITHDRAWN
import software.setixx.chimu.domain.model.Registration

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TeamCard(
    registrations: List<Registration>,
    onApprove: ((teamId: String) -> Unit)? = null,
    onReject: ((teamId: String) -> Unit)? = null,
    onDisqualify: ((teamId: String) -> Unit)? = null,
    isActionsVisible: Boolean
){
    var isJudgesExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(),
        onClick = { isJudgesExpanded = !isJudgesExpanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .padding(end = 8.dp, top = 4.dp, bottom = 4.dp, start = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Groups,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(Res.string.list_title_with_count, stringResource(Res.string.management_registrations_title), registrations.size),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Icon(
                        imageVector = if (isJudgesExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                        contentDescription = stringResource(Res.string.management_registrations_expand_desc),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = isJudgesExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
                ) {
                    if (registrations.isEmpty()) {
                        Text(
                            text = stringResource(Res.string.management_registrations_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        registrations.forEachIndexed { index, reg ->
                            SegmentedListItem(
                                colors = ListItemDefaults.colors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                ),
                                selected = false,
                                onClick = {},
                                shapes = if (registrations.size == 1) ListItemDefaults.shapes(shape = MaterialTheme.shapes.medium)
                                else ListItemDefaults.segmentedShapes(index = index, registrations.size),
                                content = { Text(reg.teamName) },
                                supportingContent = {
                                    Text(stringResource(Res.string.management_registered_by, reg.registeredByNickname))
                                },
                                trailingContent = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            color = registrationStatusColor(reg.status),
                                            shape = MaterialTheme.shapes.small
                                        ) {
                                            Text(
                                                reg.status.localize(),
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        if (isActionsVisible){
                                            when (reg.status){
                                                RegistrationStatus.PENDING -> {
                                                    IconButton(
                                                        onClick = { onApprove?.invoke(reg.teamId) }
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = stringResource(Res.string.management_approve_desc),
                                                            tint = MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                    IconButton(
                                                        onClick = { onReject?.invoke(reg.teamId) }
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Close,
                                                            contentDescription = stringResource(Res.string.management_reject_desc),
                                                            tint = MaterialTheme.colorScheme.error
                                                        )
                                                    }
                                                }
                                                RegistrationStatus.APPROVED -> {
                                                    IconButton(
                                                        onClick = { onDisqualify?.invoke(reg.teamId) }
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Block,
                                                            contentDescription = stringResource(Res.string.management_disqualify_desc),
                                                            tint = MaterialTheme.colorScheme.error
                                                        )
                                                    }
                                                }
                                                else -> {}
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RegistrationStatus.localize(): String {
    return when (this) {
        CANCELLED -> stringResource(Res.string.registration_status_cancelled)
        WITHDRAWN -> stringResource(Res.string.registration_status_withdrawn)
        APPROVED -> stringResource(Res.string.registration_status_approved)
        REJECTED -> stringResource(Res.string.registration_status_rejected)
        PENDING -> stringResource(Res.string.registration_status_pending)
        DISQUALIFIED -> stringResource(Res.string.registration_status_disqualified)
    }
}


@Composable
private fun registrationStatusColor(status: RegistrationStatus) = when (status) {
    RegistrationStatus.APPROVED -> MaterialTheme.colorScheme.primaryContainer
    RegistrationStatus.REJECTED -> MaterialTheme.colorScheme.errorContainer
    RegistrationStatus.DISQUALIFIED -> MaterialTheme.colorScheme.errorContainer
    RegistrationStatus.PENDING -> MaterialTheme.colorScheme.secondaryContainer
    else -> MaterialTheme.colorScheme.surfaceVariant
}