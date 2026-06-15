package software.setixx.chimu.presentation.jam.details.forcestatus

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoubleArrow
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import software.setixx.chimu.api.domain.GameJamStatus
import software.setixx.chimu.presentation.jam.details.JamDetailsState
import software.setixx.chimu.presentation.jam.details.transfer.DialogActions
import software.setixx.chimu.presentation.jam.details.transfer.DialogIcon
import software.setixx.chimu.presentation.jam.details.transfer.DialogTitle

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ForceStatusDialog(
    state: JamDetailsState,
    onDismiss: () -> Unit,
    onStatusSelected: (GameJamStatus) -> Unit,
    onSubmit: (GameJamStatus) -> Unit,
){
    var isStatusesExpanded by remember { mutableStateOf(false) }

    BasicAlertDialog(onDismissRequest = onDismiss){
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DialogIcon(Icons.Default.DoubleArrow, MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.height(12.dp))
                DialogTitle("Смена статуса джема")
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Укажите желаемый статус джема.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Автоматическое обновление статусов выбранного джема будет отключено!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                ExposedDropdownMenuBox(
                    expanded = isStatusesExpanded,
                    onExpandedChange = { isStatusesExpanded = !isStatusesExpanded }
                ) {
                    OutlinedTextField(
                        value = state.selectedForceStatus?.name ?: "Не выбрано",
                        onValueChange = {},
                        enabled = !state.isForceStatusActionIsLoading,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = isStatusesExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
                        isError = false,
                        shape = MaterialTheme.shapes.largeIncreased
                    )

                    ExposedDropdownMenu(
                        expanded = isStatusesExpanded,
                        onDismissRequest = { isStatusesExpanded = false },
                        shape = MenuDefaults.groupShape(0, 1).shape,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        state.availableForceStatuses.forEachIndexed { index, status ->
                            val itemShape = MenuDefaults.itemShape(
                                index = index,
                                count = state.availableForceStatuses.size
                            )

                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(status.name)
                                    }
                                },
                                modifier = if (state.selectedForceStatus == status) {
                                    Modifier
                                        .padding(horizontal = 4.dp)
                                        .clip(MaterialTheme.shapes.largeIncreased)
                                        .background(MaterialTheme.colorScheme.primary)
                                } else {
                                    Modifier
                                },
                                leadingIcon = if (state.selectedForceStatus == status) {
                                    { Icon(imageVector = Icons.Default.Check, contentDescription = "Selected") }
                                } else null,
                                colors = if (state.selectedForceStatus == status) {
                                    MenuDefaults.itemColors(
                                        textColor = MaterialTheme.colorScheme.onPrimary,
                                        leadingIconColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    MenuDefaults.itemColors()
                                },
                                shape = itemShape.shape,
                                onClick = {
                                    onStatusSelected(status)
                                    isStatusesExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))
                DialogActions(
                    onDismiss = onDismiss,
                    confirmText = "Сменить статус",
                    confirmEnabled = state.selectedForceStatus != null && !state.isTransferActionLoading,
                    isLoading = state.isForceStatusActionIsLoading,
                    onConfirm = { state.selectedForceStatus?.let { onSubmit(it) } }
                )
            }
        }
    }
}