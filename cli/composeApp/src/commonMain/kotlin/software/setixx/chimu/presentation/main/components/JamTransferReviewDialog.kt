package software.setixx.chimu.presentation.main.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import chimu.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import software.setixx.chimu.domain.model.JamTransfer
import software.setixx.chimu.presentation.utils.DateTimeUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun JamTransferReviewDialog(
    transfer: JamTransfer,
    isLoading: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onDismiss: () -> Unit
) {
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(Res.string.transfer_review_title),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(16.dp))

                Card(
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainerHigh),
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.largeIncreased,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LabeledRow(label = stringResource(Res.string.transfer_review_jam_label), value = transfer.jamName)
                            LabeledRow(label = stringResource(Res.string.transfer_review_sender_label), value = transfer.senderNickname)
                        }
                        Row {
                            IconButton(
                                onClick = onReject,
                                enabled = !isLoading
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                            IconButton(
                                onClick = onAccept,
                                enabled = !isLoading,
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(Res.string.transfer_review_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(Res.string.transfer_review_expires_at),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = DateTimeUtils.formatDateTime(transfer.expiresAt),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )

                }
                Spacer(Modifier.height(24.dp))

                if (isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(12.dp))
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .align(Alignment.End)
                ) { Text(stringResource(Res.string.close)) }
            }
        }
    }
}

@Composable
private fun LabeledRow(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            "$label: ",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}