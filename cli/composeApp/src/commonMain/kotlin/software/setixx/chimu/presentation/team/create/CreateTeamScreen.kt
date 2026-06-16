package software.setixx.chimu.presentation.team.create

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import chimu.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CreateTeamScreen(
    onBack: () -> Unit,
    onTeamCreated: (String) -> Unit,
    viewModel: CreateTeamViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error, duration = SnackbarDuration.Short)
            viewModel.clearError()
        }
    }

    LaunchedEffect(state.isSuccess) {
        val createdTeamId = state.createdTeamId
        if (state.isSuccess && createdTeamId != null) {
            onTeamCreated(createdTeamId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.team_create_title)) },
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
                        stringResource(Res.string.team_section_basic_info),
                        style = MaterialTheme.typography.titleMedium
                    )

                    OutlinedTextField(
                        value = state.name,
                        onValueChange = { viewModel.updateName(it) },
                        label = { Text(stringResource(Res.string.team_name_required_label)) },
                        enabled = !state.isCreating,
                        modifier = Modifier.fillMaxWidth(),
                        isError = state.nameError != null,
                        supportingText = state.nameError?.let { { Text(it) } },
                        leadingIcon = { Icon(Icons.Default.Group, null) },
                        singleLine = true,
                        shape = MaterialTheme.shapes.largeIncreased,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        )
                    )
                    OutlinedTextField(
                        value = state.description,
                        onValueChange = { viewModel.updateDescription(it) },
                        label = { Text(stringResource(Res.string.description)) },
                        enabled = !state.isCreating,
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        leadingIcon = { Icon(Icons.Default.Description, null) },
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
                            stringResource(Res.string.team_important_info),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }

                    Text(
                        stringResource(Res.string.team_create_hint_leader),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        stringResource(Res.string.team_create_hint_limit),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        stringResource(Res.string.team_create_hint_unique),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        stringResource(Res.string.team_create_hint_token),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.createTeam() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ButtonDefaults.MediumContainerHeight),
                enabled = !state.isCreating
            ) {
                if (state.isCreating) {
                    LoadingIndicator()
                } else {
                    Text(stringResource(Res.string.team_create_title))
                }
            }
        }
    }
}



