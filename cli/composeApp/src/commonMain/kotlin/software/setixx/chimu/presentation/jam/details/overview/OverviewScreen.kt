package software.setixx.chimu.presentation.jam.details.overview

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
import software.setixx.chimu.api.domain.GameJamStatus
import software.setixx.chimu.api.domain.RegistrationStatus
import software.setixx.chimu.api.domain.UserRole
import software.setixx.chimu.domain.model.GameJamDetails
import software.setixx.chimu.presentation.jam.details.management.components.TeamCard
import software.setixx.chimu.presentation.jam.details.overview.components.JamOverviewSection
import software.setixx.chimu.presentation.main.components.JamBanner

@Composable
fun OverviewScreen(
    jamId: String,
    jam: GameJamDetails,
    userRole: UserRole?,
    userId: String?,
    onNavigateToAlienProfile: (String) -> Unit,
    onNavigateToOwnProfile: () -> Unit,
    viewModel: OverviewViewModel = koinViewModel()
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

    val isJamOrganizer = jam.organizerId == userId
    val isJamJudge = jam.judges.any { it.userId == userId }

    val activeStatuses = setOf(
        GameJamStatus.REGISTRATION_OPEN,
        GameJamStatus.REGISTRATION_CLOSED,
        GameJamStatus.IN_PROGRESS
    )

    val cancellableStatuses = setOf(
        GameJamStatus.REGISTRATION_OPEN,
        GameJamStatus.REGISTRATION_CLOSED
    )

    val withdrawnStatuses = setOf(
        GameJamStatus.IN_PROGRESS
    )

    if (state.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ){
            LoadingIndicator()
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                JamBanner(
                    status = jam.status,
                    bannerUrl = jam.bannerUrl,
                    name = jam.name,
                    theme = jam.theme
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val registeredTeam = state.registeredTeam
                    if (jam.status in activeStatuses || jam.status == GameJamStatus.ANNOUNCED) {
                        when {
                            isJamOrganizer -> {
                                Button(
                                    onClick = {},
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = false
                                ) {
                                    Text("Вы организатор джема")
                                }
                            }

                            isJamJudge -> {
                                Button(
                                    onClick = {},
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = false
                                ) {
                                    Text("Вы судья джема")
                                }
                            }

                            registeredTeam != null -> {
                                OutlinedButton(
                                    onClick = { viewModel.withdrawTeam(jamId, registeredTeam.id) },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !state.isActionLoading && state.isLeaderOfRegisteredTeam
                                ) {
                                    if (state.isActionLoading) {
                                        LoadingIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    } else {
                                        Icon(Icons.Default.Undo, null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    Text(
                                        if (state.isLeaderOfRegisteredTeam && jam.status in cancellableStatuses) {
                                            "Отменить заявку: ${registeredTeam.name}"
                                        } else if (state.isLeaderOfRegisteredTeam && jam.status in withdrawnStatuses){
                                            "Снять с джема: ${registeredTeam.name}"
                                        } else {
                                            "Заявка подана: ${registeredTeam.name}"
                                        }
                                    )
                                }
                            }

                            state.canTeamRegister(jam) -> {
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

                    userId?.let {
                        JamOverviewSection(
                            jam = jam,
                            userId = it,
                            onNavigateToOwnProfile = onNavigateToOwnProfile,
                            onNavigateToAlienProfile = { userId ->
                                onNavigateToAlienProfile(userId)
                            }
                        )
                    }

                    TeamCard(
                        registrations = state.registrations,
                        isActionsVisible = false
                    )

                    Spacer(modifier = Modifier.height(80.dp))
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
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
