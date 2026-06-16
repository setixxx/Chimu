package software.setixx.chimu.presentation.jam.edit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import chimu.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import org.koin.compose.viewmodel.koinViewModel
import software.setixx.chimu.data.picker.rememberFilePicker
import software.setixx.chimu.presentation.jam.create.components.DateTimePickerField
import software.setixx.chimu.presentation.jam.create.components.JamSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditJamScreen(
    jamId: String,
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: EditJamViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(jamId) {
        viewModel.loadJam(jamId)
    }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onSuccess()
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.jam_edit_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, stringResource(Res.string.back))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
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
                        supportingText = state.nameError?.let { { Text(it) } }
                    )

                    OutlinedTextField(
                        value = state.description,
                        onValueChange = { viewModel.onDescriptionChange(it) },
                        label = { Text(stringResource(Res.string.description)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    OutlinedTextField(
                        value = state.theme,
                        onValueChange = { viewModel.onThemeChange(it) },
                        label = { Text(stringResource(Res.string.jam)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = state.rules,
                        onValueChange = { viewModel.onRulesChange(it) },
                        label = { Text(stringResource(Res.string.rules)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }

                JamSection(title = stringResource(Res.string.jam_section_dates)) {
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
                            label = { Text(stringResource(Res.string.jam_min_size_label)) },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = state.maxTeamSize,
                            onValueChange = { viewModel.onMaxTeamSizeChange(it) },
                            label = { Text(stringResource(Res.string.jam_max_size_label)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Button(
                    onClick = { viewModel.updateJam() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = !state.isUpdating
                ) {
                    if (state.isUpdating) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Text(stringResource(Res.string.jam_save_changes_button))
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}