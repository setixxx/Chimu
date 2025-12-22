package software.setixx.chimu.presentation.auth.register

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import chimu.composeapp.generated.resources.Res
import chimu.composeapp.generated.resources.email_hint
import chimu.composeapp.generated.resources.hide_password_hint
import chimu.composeapp.generated.resources.password_confirm_hint
import chimu.composeapp.generated.resources.password_hint
import chimu.composeapp.generated.resources.show_password_hint
import chimu.composeapp.generated.resources.sign_in_button
import chimu.composeapp.generated.resources.sign_in_title
import chimu.composeapp.generated.resources.sign_up_button
import chimu.composeapp.generated.resources.sign_up_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.Float

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: RegisterViewModel = koinViewModel(),
    platformSizeModifier: Float
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.5f + platformSizeModifier),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(Res.string.sign_up_title),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(MaterialTheme.shapes.extraLarge),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = state.email,
                            onValueChange = viewModel::onEmailChange,
                            label = { Text(stringResource(Res.string.email_hint)) },
                            singleLine = true,
                            isError = state.emailError != null,
                            supportingText = state.emailError?.let { { Text(it) } },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.isLoading
                        )

                        OutlinedTextField(
                            value = state.password,
                            onValueChange = viewModel::onPasswordChange,
                            label = { Text(stringResource(Res.string.password_hint)) },
                            singleLine = true,
                            isError = state.passwordError != null,
                            supportingText = state.passwordError?.let { { Text(it) } },
                            visualTransformation = if (state.isPasswordVisible)
                                VisualTransformation.None
                            else
                                PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = viewModel::togglePasswordVisibility) {
                                    Icon(
                                        imageVector = if (state.isPasswordVisible)
                                            Icons.Default.Visibility
                                        else
                                            Icons.Default.VisibilityOff,
                                        contentDescription = if (state.isPasswordVisible)
                                            stringResource(Res.string.hide_password_hint)
                                        else
                                            stringResource(Res.string.show_password_hint)
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.isLoading
                        )

                        if (state.password.isNotEmpty()) {
                            PasswordStrengthIndicator(strength = state.passwordStrength)
                        }

                        OutlinedTextField(
                            value = state.confirmPassword,
                            onValueChange = viewModel::onConfirmPasswordChange,
                            label = { Text(stringResource(Res.string.password_confirm_hint)) },
                            singleLine = true,
                            isError = state.confirmPasswordError != null,
                            supportingText = state.confirmPasswordError?.let { { Text(it) } },
                            visualTransformation = if (state.isConfirmPasswordVisible)
                                VisualTransformation.None
                            else
                                PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = viewModel::toggleConfirmPasswordVisibility) {
                                    Icon(
                                        imageVector = if (state.isConfirmPasswordVisible)
                                            Icons.Default.Visibility
                                        else
                                            Icons.Default.VisibilityOff,
                                        contentDescription = if (state.isConfirmPasswordVisible)
                                            stringResource(Res.string.hide_password_hint)
                                        else
                                            stringResource(Res.string.show_password_hint)
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { viewModel.onRegisterClick(onRegisterSuccess) }
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.isLoading
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { viewModel.onRegisterClick(onRegisterSuccess) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            enabled = !state.isLoading
                        ) {
                            if (state.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text(stringResource(Res.string.sign_up_button))
                            }
                        }

                        OutlinedButton(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            onClick = onNavigateToLogin,
                            enabled = !state.isLoading
                        ) {
                            Text(stringResource(Res.string.sign_in_button))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PasswordStrengthIndicator(strength: PasswordStrength) {
    val (color, text) = when (strength) {
        PasswordStrength.WEAK -> Color(0xFFEF5350) to "Слабый пароль"
        PasswordStrength.MEDIUM -> Color(0xFFFFA726) to "Средний пароль"
        PasswordStrength.STRONG -> Color(0xFF66BB6A) to "Надежный пароль"
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        LinearProgressIndicator(
            progress = { when (strength) {
                PasswordStrength.WEAK -> 0.33f
                PasswordStrength.MEDIUM -> 0.66f
                PasswordStrength.STRONG -> 1f
            } },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = color,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = color
        )
    }
}