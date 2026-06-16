package software.setixx.chimu.presentation.jam.create

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import chimu.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import software.setixx.chimu.api.domain.UserRole
import software.setixx.chimu.presentation.jam.create.components.DateTimePickerField
import software.setixx.chimu.presentation.jam.create.components.JamSection

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CreateJamScreen(
    onBack: () -> Unit,
    onJamCreated: (String) -> Unit,
    viewModel: CreateJamViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error, duration = SnackbarDuration.Short)
            viewModel.clearError()
        }
    }

    LaunchedEffect(state.isSuccess) {
        val createdJamId = state.createdJamId
        if (state.isSuccess && createdJamId != null) {
            onJamCreated(createdJamId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.jam_create_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, stringResource(Res.string.back))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (state.userRole != UserRole.ADMIN && state.userRole != UserRole.ORGANIZER && state.userRole != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(stringResource(Res.string.jam_create_no_permission), style = MaterialTheme.typography.titleLarge)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                JamSection(title = stringResource(Res.string.jam_section_basic_info)) {
                    OutlinedTextField(
                        value = state.name,
                        onValueChange = { viewModel.onNameChange(it) },
                        label = { Text(stringResource(Res.string.jam_name_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        isError = state.nameError != null,
                        supportingText = state.nameError?.let { { Text(it) } },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        shape = MaterialTheme.shapes.largeIncreased
                    )

                    OutlinedTextField(
                        value = state.description,
                        onValueChange = { viewModel.onDescriptionChange(it) },
                        label = { Text(stringResource(Res.string.jam_description_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        shape = MaterialTheme.shapes.largeIncreased
                    )

                    OutlinedTextField(
                        value = state.theme,
                        onValueChange = { viewModel.onThemeChange(it) },
                        label = { Text(stringResource(Res.string.jam_theme_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        shape = MaterialTheme.shapes.largeIncreased
                    )

                    OutlinedTextField(
                        value = state.rules,
                        onValueChange = { viewModel.onRulesChange(it) },
                        label = { Text(stringResource(Res.string.jam_rules_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        shape = MaterialTheme.shapes.largeIncreased
                    )
                }

                JamSection(title = stringResource(Res.string.jam_section_dates)) {
                    if (state.dateError != null) {
                        Text(
                            text = state.dateError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    DateTimePickerField(
                        label = stringResource(Res.string.jam_date_reg_start),
                        value = state.registrationStart,
                        onValueChange = { viewModel.onRegistrationStartChange(it) }
                    )

                    DateTimePickerField(
                        label = stringResource(Res.string.jam_date_reg_end),
                        value = state.registrationEnd,
                        onValueChange = { viewModel.onRegistrationEndChange(it) }
                    )

                    DateTimePickerField(
                        label = stringResource(Res.string.jam_date_jam_start),
                        value = state.jamStart,
                        onValueChange = { viewModel.onJamStartChange(it) }
                    )

                    DateTimePickerField(
                        label = stringResource(Res.string.jam_date_jam_end),
                        value = state.jamEnd,
                        onValueChange = { viewModel.onJamEndChange(it) }
                    )

                    DateTimePickerField(
                        label = stringResource(Res.string.jam_date_judging_start),
                        value = state.judgingStart,
                        onValueChange = { viewModel.onJudgingStartChange(it) }
                    )

                    DateTimePickerField(
                        label = stringResource(Res.string.jam_date_judging_end),
                        value = state.judgingEnd,
                        onValueChange = { viewModel.onJudgingEndChange(it) }
                    )
                }

                JamSection(title = stringResource(Res.string.jam_section_teams)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = state.minTeamSize,
                            onValueChange = { viewModel.onMinTeamSizeChange(it) },
                            label = { Text(stringResource(Res.string.jam_min_team_size)) },
                            modifier = Modifier.weight(1f),
                            isError = state.teamSizeError != null,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            shape = MaterialTheme.shapes.largeIncreased
                        )

                        OutlinedTextField(
                            value = state.maxTeamSize,
                            onValueChange = { viewModel.onMaxTeamSizeChange(it) },
                            label = { Text(stringResource(Res.string.jam_max_team_size)) },
                            modifier = Modifier.weight(1f),
                            isError = state.teamSizeError != null,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            shape = MaterialTheme.shapes.largeIncreased
                        )
                    }
                    if (state.teamSizeError != null) {
                        Text(
                            text = state.teamSizeError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Button(
                    onClick = { viewModel.createJam() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ButtonDefaults.MediumContainerHeight),
                    enabled = !state.isLoading
                ) {
                    if (state.isLoading) {
                        LoadingIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(stringResource(Res.string.jam_create_button))
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
