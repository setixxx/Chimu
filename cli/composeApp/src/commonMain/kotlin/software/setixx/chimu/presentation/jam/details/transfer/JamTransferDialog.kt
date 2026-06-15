package software.setixx.chimu.presentation.jam.details.transfer

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import software.setixx.chimu.api.domain.TransferStatus
import software.setixx.chimu.domain.model.JamTransfer
import software.setixx.chimu.domain.model.PublicUserProfile
import software.setixx.chimu.presentation.jam.details.JamDetailsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JamTransferDialog(
    state: JamDetailsState,
    onDismiss: () -> Unit,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onCreate: () -> Unit,
    onCancel: () -> Unit
) {
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            AnimatedContent(
                targetState = resolveMode(state.currentTransfer),
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "transfer_mode"
            ) { mode ->
                when (mode) {
                    Mode.CREATE -> CreateContent(
                        state = state,
                        onQueryChange = onQueryChange,
                        onSearch = onSearch,
                        onCreate = onCreate,
                        onDismiss = onDismiss
                    )
                    Mode.PENDING -> PendingContent(
                        transfer = state.currentTransfer!!,
                        isLoading = state.isTransferActionLoading,
                        onCancel = onCancel,
                        onDismiss = onDismiss
                    )
                    Mode.DONE -> DoneContent(
                        transfer = state.currentTransfer!!,
                        onDismiss = onDismiss,
                        isOrganizer = state.userId == state.jamDetails?.organizerId
                    )
                }
            }
        }
    }
}

private enum class Mode { CREATE, PENDING, DONE }

private fun resolveMode(transfer: JamTransfer?): Mode = when {
    transfer == null || transfer.status == TransferStatus.CANCELLED -> Mode.CREATE
    transfer.status == TransferStatus.PENDING -> Mode.PENDING
    else -> Mode.DONE
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CreateContent(
    state: JamDetailsState,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onCreate: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier.padding(24.dp).animateContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DialogIcon(Icons.Default.SwapHoriz, MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.height(12.dp))
        DialogTitle("Передать джем")
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Введите никнейм пользователя, которому хотите передать права организатора.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = state.transferRecipientQuery,
            onValueChange = onQueryChange,
            label = { Text("Никнейм пользователя") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                if (state.isSearchingRecipient) {
                    LoadingIndicator(modifier = Modifier.size(24.dp))
                } else {
                    IconButton(
                        onClick = onSearch,
                        enabled = state.transferRecipientQuery.isNotBlank()
                    ) {
                        Icon(Icons.Default.Search, "Найти")
                    }
                }
            },
            isError = state.transferError != null,
            supportingText = state.transferError?.let { err -> { Text(err) } },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            shape = MaterialTheme.shapes.largeIncreased
        )

        state.transferRecipientFound?.let { user ->
            Spacer(Modifier.height(12.dp))
            RecipientCard(user)
        }

        Spacer(Modifier.height(24.dp))
        DialogActions(
            onDismiss = onDismiss,
            confirmText = "Отправить заявку",
            confirmEnabled = state.transferRecipientFound != null && !state.isTransferActionLoading,
            isLoading = state.isTransferActionLoading,
            onConfirm = onCreate
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RecipientCard(user: PublicUserProfile) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        shape = MaterialTheme.shapes.largeIncreased
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.nickname,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                val fullName = listOfNotNull(user.firstName, user.lastName).joinToString(" ")
                if (fullName.isNotBlank()) {
                    Text(
                        text = fullName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
            Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}


@Composable
private fun PendingContent(
    transfer: JamTransfer,
    isLoading: Boolean,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DialogIcon(Icons.Default.HourglassTop, MaterialTheme.colorScheme.tertiary)
        Spacer(Modifier.height(12.dp))
        DialogTitle("Заявка отправлена")
        Spacer(Modifier.height(16.dp))

        InfoRow("Получатель", transfer.recipientNickname)
        Spacer(Modifier.height(8.dp))
        HorizontalDivider()
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Заявка ожидает принятия. Если получатель примет её — права организатора перейдут к нему.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onDismiss) { Text("Закрыть") }
            Spacer(Modifier.width(8.dp))
            FilledTonalButton(
                onClick = onCancel,
                enabled = !isLoading,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Cancel, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Отозвать")
                }
            }
        }
    }
}


@Composable
private fun DoneContent(transfer: JamTransfer, onDismiss: () -> Unit, isOrganizer: Boolean) {
    val icon = when (transfer.status) {
        TransferStatus.ACCEPTED -> Icons.Default.CheckCircle
        TransferStatus.REJECTED -> Icons.Default.Cancel
        else -> Icons.Default.Info
    }
    val tint = when (transfer.status) {
        TransferStatus.ACCEPTED -> MaterialTheme.colorScheme.primary
        TransferStatus.REJECTED -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val title = when (transfer.status) {
        TransferStatus.ACCEPTED -> if (isOrganizer) "Джем получен" else "Джем передан"
        TransferStatus.REJECTED -> "Заявка отклонена"
        else -> "Заявка отменена"
    }
    val body = when (transfer.status) {
        TransferStatus.ACCEPTED -> if (isOrganizer){
            "Джем получен от ${transfer.recipientNickname}. Права переданы."
        } else {
            "${transfer.recipientNickname} принял заявку. Права переданы."
        }
        TransferStatus.REJECTED -> "${transfer.recipientNickname} отклонил заявку."
        else -> "Заявка на передачу была отменена."
    }

    Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DialogIcon(icon, tint)
        Spacer(Modifier.height(12.dp))
        DialogTitle(title)
        Spacer(Modifier.height(12.dp))
        Text(text = body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onDismiss) { Text("Закрыть") }
        }
    }
}


@Composable
fun DialogIcon(icon: ImageVector, tint: Color) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = tint,
        modifier = Modifier.size(24.dp)
    )
}

@Composable
fun ColumnScope.DialogTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.align(Alignment.CenterHorizontally)
    )
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            "$label:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(8.dp))
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun DialogActions(
    onDismiss: () -> Unit,
    confirmText: String,
    confirmEnabled: Boolean,
    isLoading: Boolean,
    onConfirm: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onDismiss) { Text("Отмена") }
        Spacer(Modifier.width(8.dp))
        Button(onClick = onConfirm, enabled = confirmEnabled) {
            if (isLoading) {
                LoadingIndicator(
                    modifier = Modifier.size(24.dp),
                )
            } else {
                Text(confirmText)
            }
        }
    }
}