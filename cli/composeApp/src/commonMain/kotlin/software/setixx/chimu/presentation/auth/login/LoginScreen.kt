package software.setixx.chimu.presentation.auth.login

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import chimu.composeapp.generated.resources.Res
import chimu.composeapp.generated.resources.email_hint
import chimu.composeapp.generated.resources.hide_password_hint
import chimu.composeapp.generated.resources.password_hint
import chimu.composeapp.generated.resources.remember_user_title
import chimu.composeapp.generated.resources.show_password_hint
import chimu.composeapp.generated.resources.sign_in_button
import chimu.composeapp.generated.resources.sign_in_title
import chimu.composeapp.generated.resources.sign_up_button
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = koinViewModel(),
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
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ){
                Text(
                    text = stringResource(Res.string.sign_in_title),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
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
                        enabled = !state.isLoading,
                        shape = MaterialTheme.shapes.largeIncreased
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
                                        Icons.Filled.Visibility
                                    else
                                        Icons.Filled.VisibilityOff,
                                    contentDescription = if (state.isPasswordVisible)
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
                            onDone = { viewModel.onLoginClick(onLoginSuccess) }
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoading,
                        shape = MaterialTheme.shapes.largeIncreased
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = state.rememberMe,
                            onCheckedChange = viewModel::onRememberMeChange,
                            enabled = !state.isLoading
                        )
                        Text(
                            text = stringResource(Res.string.remember_user_title),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Button(
                        onClick = { viewModel.onLoginClick(onLoginSuccess) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(ButtonDefaults.MediumContainerHeight),
                        enabled = !state.isLoading
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(stringResource(Res.string.sign_in_button))
                        }
                    }

                    OutlinedButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(ButtonDefaults.MediumContainerHeight),
                        onClick = onNavigateToRegister,
                        enabled = !state.isLoading
                    ) {
                        Text(stringResource(Res.string.sign_up_button))
                    }
                }
            }
        }
    }
}