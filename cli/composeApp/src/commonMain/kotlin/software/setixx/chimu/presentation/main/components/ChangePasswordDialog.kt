package software.setixx.chimu.presentation.main.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import chimu.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import software.setixx.chimu.presentation.components.PasswordStrengthIndicator
import software.setixx.chimu.presentation.utils.PasswordUtils

@Composable
fun ChangePasswordDialog(
    isLoading: Boolean,
    errorMessage: String?,
    onSubmit: (oldPassword: String, newPassword: String, confirmPassword: String) -> Unit,
    onDismiss: () -> Unit
) {
    var oldPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordsVisible by rememberSaveable { mutableStateOf(false) }

    val newPasswordStrength = remember(newPassword) {
        PasswordUtils.calculatePasswordStrength(newPassword)
    }

    AlertDialog(
        onDismissRequest = {
            if (!isLoading) {
                onDismiss()
            }
        },
        title = { Text(stringResource(Res.string.main_change_password_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PasswordField(
                    value = oldPassword,
                    onValueChange = { oldPassword = it },
                    label = stringResource(Res.string.main_current_password_label),
                    passwordsVisible = passwordsVisible,
                    onToggleVisibility = { passwordsVisible = !passwordsVisible },
                    enabled = !isLoading
                )
                PasswordField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = stringResource(Res.string.main_new_password_label),
                    passwordsVisible = passwordsVisible,
                    onToggleVisibility = { passwordsVisible = !passwordsVisible },
                    enabled = !isLoading
                )
                if (newPassword.isNotEmpty()) {
                    PasswordStrengthIndicator(strength = newPasswordStrength)
                }
                PasswordField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = stringResource(Res.string.main_confirm_new_password_label),
                    passwordsVisible = passwordsVisible,
                    onToggleVisibility = { passwordsVisible = !passwordsVisible },
                    enabled = !isLoading
                )

                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(oldPassword, newPassword, confirmPassword) },
                enabled = !isLoading
            ) {
                if (isLoading) {
                    LoadingIndicator(
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Text(stringResource(Res.string.main_change_button))
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text(stringResource(Res.string.cancel))
            }
        }
    )
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    passwordsVisible: Boolean,
    onToggleVisibility: () -> Unit,
    enabled: Boolean
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        visualTransformation = if (passwordsVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        trailingIcon = {
            IconButton(onClick = onToggleVisibility) {
                Icon(
                    imageVector = if (passwordsVisible) {
                        Icons.Filled.VisibilityOff
                    } else {
                        Icons.Filled.Visibility
                    },
                    contentDescription = if (passwordsVisible) {
                        stringResource(Res.string.hide_password_hint)
                    } else {
                        stringResource(Res.string.show_password_hint)
                    }
                )
            }
        },
        enabled = enabled,
        modifier = Modifier.fillMaxWidth()
    )
}
