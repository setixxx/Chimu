package software.setixx.chimu.presentation.profile.roleupgrade

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import chimu.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import software.setixx.chimu.api.domain.UserRole
import software.setixx.chimu.presentation.profile.roleupgrade.components.AdminContent
import software.setixx.chimu.presentation.profile.roleupgrade.components.CreateRequestContent
import software.setixx.chimu.presentation.profile.roleupgrade.components.UserListContent

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RoleUpgradeDialog(
    currentRole: UserRole,
    isAdmin: Boolean,
    viewModel: RoleUpgradeViewModel,
    onDismiss: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        if (isAdmin) viewModel.loadForAdmin() else viewModel.loadForUser()
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.clearError()
        }
    }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.clearSuccess()
        }
    }

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .widthIn(min = 280.dp, max = 560.dp)
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Box {
                val screenKey = when {
                    isAdmin -> "admin"
                    state.showCreateForm -> "create"
                    else -> "user"
                }

                AnimatedContent(
                    targetState = screenKey,
                    transitionSpec = {
                        if (targetState == "create") {
                            (slideInHorizontally { it } + fadeIn()) togetherWith
                                    (slideOutHorizontally { -it } + fadeOut())
                        } else {
                            (slideInHorizontally { -it } + fadeIn()) togetherWith
                                    (slideOutHorizontally { it } + fadeOut())
                        }
                    },
                    label = "role_upgrade_screen"
                ) { screen ->
                    when (screen) {
                        "create" -> CreateRequestContent(state = state, viewModel = viewModel, currentRole = currentRole)
                        "admin" -> AdminContent(state = state, viewModel = viewModel, onDismiss = onDismiss)
                        else -> UserListContent(state = state, viewModel = viewModel, onDismiss = onDismiss)
                    }
                }

                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

@Composable
fun String.toRoleDisplayName(): String = when (this) {
    "ORGANIZER" -> stringResource(Res.string.role_upgrade_organizer)
    "JUDGE" -> stringResource(Res.string.role_upgrade_judge)
    else -> this
}