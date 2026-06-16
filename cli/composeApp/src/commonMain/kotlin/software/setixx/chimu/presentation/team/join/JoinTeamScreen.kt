package software.setixx.chimu.presentation.team.join

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import chimu.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import software.setixx.chimu.presentation.team.details.TeamDetailsViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun JoinTeamScreen(
    onBack: () -> Unit,
    onTeamJoined: (String) -> Unit,
    viewModel: TeamDetailsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error, duration = SnackbarDuration.Short)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.team_join_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, stringResource(Res.string.back))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        stringResource(Res.string.team_join_token_hint),
                        style = MaterialTheme.typography.titleMedium
                    )

                    OutlinedTextField(
                        value = state.inviteToken,
                        onValueChange = { viewModel.updateInviteToken(it) },
                        label = { Text(stringResource(Res.string.team_invite_title)) },
                        enabled = !state.isLoading,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Key, null) },
                        singleLine = true,
                        shape = MaterialTheme.shapes.largeIncreased
                    )
                }
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Info,
                            null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            stringResource(Res.string.team_how_it_works),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }

                    Text(
                        stringResource(Res.string.team_join_step_1),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        stringResource(Res.string.team_join_step_2),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        stringResource(Res.string.team_join_step_3),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.joinTeam(onTeamJoined) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ButtonDefaults.MediumContainerHeight),
                enabled = !state.isLoading && state.inviteToken.isNotBlank()
            ) {
                if (state.isLoading) {
                    LoadingIndicator()
                    Spacer(modifier = Modifier.width(8.dp))
                } else {
                    Text(stringResource(Res.string.teams_join))
                }
            }
        }
    }
}