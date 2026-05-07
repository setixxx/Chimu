package software.setixx.chimu.presentation.jam.details.registration

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import software.setixx.chimu.api.domain.RegistrationStatus
import software.setixx.chimu.api.domain.UserRole
import software.setixx.chimu.domain.model.GameJamDetails
import software.setixx.chimu.presentation.components.JamOverviewSection
import software.setixx.chimu.presentation.components.RegisteredTeamsSection

@Composable
fun RegistrationScreen(
    jamId: String,
    jam: GameJamDetails,
    userRole: UserRole?,
    viewModel: RegistrationViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showRegisterDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    LaunchedEffect(jamId) {
        viewModel.load(jamId)
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    val isParticipant = userRole == UserRole.PARTICIPANT

    if (!isParticipant) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "Регистрация доступна только участникам",
                style = MaterialTheme.typography.titleMedium
            )
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            JamOverviewSection(jam = jam)

            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                RegisteredTeamsSection(
                    registrations = state.registrations.filter { it.status == RegistrationStatus.APPROVED }
                )
            }

            if (state.userTeams.isNotEmpty()) {
                val registeredTeams = state.userTeams.filter { state.isTeamRegistered(it.id) }
                registeredTeams.forEach { team ->
                    OutlinedButton(
                        onClick = { viewModel.withdrawTeam(jamId, team.id) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isActionLoading
                    ) {
                        Icon(Icons.Default.Undo, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Отменить заявку: ${team.name}")
                    }
                }
                if (state.canTeamRegister(jam)){
                    Button(
                        onClick = { showRegisterDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isActionLoading
                    ) {
                        if (state.isActionLoading) {
                            LoadingIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Подать заявку на участие")
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (showRegisterDialog) {
        AlertDialog(
            onDismissRequest = { showRegisterDialog = false },
            title = { Text("Выберите команду") },
            text = {
                Column {
                    state.userTeams.forEach { team ->
                        val isRegistered = state.isTeamRegistered(team.id)
                        ListItem(
                            headlineContent = { Text(team.name) },
                            supportingContent = {
                                Text(
                                    if (isRegistered) "Уже зарегистрирована"
                                    else "${team.memberCount} участников"
                                )
                            },
                            trailingContent = {
                                if (!isRegistered) {
                                    Button(
                                        onClick = {
                                            viewModel.registerTeam(jamId, team.id)
                                            showRegisterDialog = false
                                        },
                                        enabled = !state.isActionLoading
                                    ) {
                                        Text("Выбрать")
                                    }
                                }
                            }
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showRegisterDialog = false }) { Text("Отмена") }
            }
        )
    }
}
