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
import software.setixx.chimu.api.domain.UserRole
import software.setixx.chimu.domain.model.GameJamDetails
import software.setixx.chimu.presentation.components.JamOverviewSection
import software.setixx.chimu.presentation.components.RegisteredTeamsSection

@Composable
fun RegistrationScreen(
    jamId: String,
    jam: GameJamDetails,
    userRole: UserRole?,
    userId: String?,
    viewModel: RegistrationViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showRegisterDialog by remember { mutableStateOf(false) }
    var showAssignJudgeDialog by remember { mutableStateOf(false) }
    var judgeUserIdInput by remember { mutableStateOf("") }
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

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccess()
        }
    }

    val isAdminOrOrganizer = userRole == UserRole.ADMIN ||
            (userRole == UserRole.ORGANIZER && jam.organizerId == userId)
    val isParticipant = userRole == UserRole.PARTICIPANT

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

    if (showAssignJudgeDialog) {
        AlertDialog(
            onDismissRequest = { showAssignJudgeDialog = false },
            title = { Text("Назначить судью") },
            text = {
                OutlinedTextField(
                    value = judgeUserIdInput,
                    onValueChange = { judgeUserIdInput = it },
                    label = { Text("ID пользователя") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.assignJudge(jamId, judgeUserIdInput)
                        judgeUserIdInput = ""
                        showAssignJudgeDialog = false
                    },
                    enabled = judgeUserIdInput.isNotBlank() && !state.isActionLoading
                ) { Text("Назначить") }
            },
            dismissButton = {
                TextButton(onClick = { showAssignJudgeDialog = false }) { Text("Отмена") }
            }
        )
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
                    registrations = if (isAdminOrOrganizer) {
                        state.registrations
                    } else {
                        state.registrations.filter { it.status == "APPROVED" }
                    },
                    actions = { reg ->
                        if (isAdminOrOrganizer) {
                            Row {
                                if (reg.status == "PENDING") {
                                    IconButton(
                                        onClick = { viewModel.updateRegistrationStatus(jamId, reg.teamId, "APPROVED") },
                                        enabled = !state.isActionLoading
                                    ) {
                                        Icon(
                                            Icons.Default.Check,
                                            "Одобрить",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.updateRegistrationStatus(jamId, reg.teamId, "REJECTED") },
                                        enabled = !state.isActionLoading
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            "Отклонить",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                                if (reg.status != "DISQUALIFIED") {
                                    IconButton(
                                        onClick = { viewModel.updateRegistrationStatus(jamId, reg.teamId, "DISQUALIFIED") },
                                        enabled = !state.isActionLoading
                                    ) {
                                        Icon(
                                            Icons.Default.Block,
                                            "Дисквалифицировать",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                )
            }

            if (isParticipant && state.userTeams.isNotEmpty()) {
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
                Button(
                    onClick = { showRegisterDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isActionLoading
                ) {
                    if (state.isActionLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Подать заявку на участие")
                }
            }

            if (isAdminOrOrganizer) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Управление судьями (${state.judges.size})", style = MaterialTheme.typography.titleMedium)
                            TextButton(onClick = { showAssignJudgeDialog = true }) {
                                Icon(Icons.Default.PersonAdd, null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Назначить")
                            }
                        }

                        if (state.judges.isEmpty()) {
                            Text("Судьи не назначены.")
                        } else {
                            state.judges.forEachIndexed { index, judge ->
                                ListItem(
                                    headlineContent = { Text(judge.nickname) },
                                    supportingContent = { Text("Назначен: ${judge.assignedAt}") },
                                    trailingContent = {
                                        IconButton(
                                            onClick = { viewModel.unassignJudge(jamId, judge.userId) },
                                            enabled = !state.isActionLoading
                                        ) {
                                            Icon(
                                                Icons.Default.PersonRemove,
                                                "Снять",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                )
                                if (index < state.judges.lastIndex) HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
