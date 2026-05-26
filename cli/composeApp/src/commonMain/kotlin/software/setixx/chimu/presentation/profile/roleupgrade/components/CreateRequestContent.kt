package software.setixx.chimu.presentation.profile.roleupgrade.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import software.setixx.chimu.api.domain.UserRole
import software.setixx.chimu.presentation.profile.roleupgrade.RoleUpgradeState
import software.setixx.chimu.presentation.profile.roleupgrade.RoleUpgradeViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CreateRequestContent(
    currentRole: UserRole,
    state: RoleUpgradeState,
    viewModel: RoleUpgradeViewModel
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .padding(top = 16.dp, bottom = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 20.dp)
        ) {
            IconButton(
                onClick = { viewModel.hideCreateForm() },
                enabled = !state.isSubmitting
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
            }
            Text(
                "Новая заявка",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        Text(
            "Желаемая роль",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val availableRoles = listOf(
            "Организатор" to UserRole.ORGANIZER,
            "Судья" to UserRole.JUDGE
        )


        val icons = listOf(
            Icons.Default.Event,
            Icons.Default.Gavel
        )

        BoxWithConstraints {
            val isNarrow = maxWidth < 280.dp
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                maxItemsInEachRow = if (isNarrow) 1 else Int.MAX_VALUE
            ) {
                availableRoles.forEachIndexed { index, (label, userRole) ->
                    val isSelected = state.selectedRole == userRole
                    ToggleButton(
                        enabled = userRole != currentRole,
                        checked = isSelected,
                        onCheckedChange = {
                            viewModel.selectRole(userRole)
                        },
                        shapes = if (isNarrow) {
                            ToggleButtonDefaults.shapesFor(ButtonDefaults.MinHeight)
                        } else {
                            when (index) {
                                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                availableRoles.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                            }
                        },
                        modifier = Modifier
                            .semantics { role = Role.RadioButton }
                            .weight(1f),
                    ) {
                        if (isSelected) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                            )
                            Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                        } else {
                            Icon(
                                icons[index],
                                contentDescription = null
                            )
                        }
                        Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                        Text(label)
                    }
                }
            }
        }

        OutlinedTextField(
            value = state.userMessage,
            onValueChange = { viewModel.updateUserMessage(it) },
            label = { Text("Причина (необязательно)") },
            placeholder = { Text("Расскажите, почему вы хотите получить эту роль...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5,
            shape = MaterialTheme.shapes.large,
            enabled = !state.isSubmitting
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = { viewModel.hideCreateForm() },
                enabled = !state.isSubmitting
            ) {
                Text("Отмена")
            }
            Button(
                onClick = { viewModel.submitRequest() },
                enabled = state.selectedRole != null && !state.isSubmitting
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                    Text("Отправить")
                }
            }
        }
    }
}