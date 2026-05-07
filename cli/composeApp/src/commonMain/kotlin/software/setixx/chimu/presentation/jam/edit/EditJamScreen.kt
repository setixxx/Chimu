package software.setixx.chimu.presentation.jam.edit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
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
                title = { Text("Редактировать джем") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Назад")
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
                JamSection(title = "Основная информация") {
                    OutlinedTextField(
                        value = state.name,
                        onValueChange = { viewModel.onNameChange(it) },
                        label = { Text("Название *") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = state.nameError != null,
                        supportingText = state.nameError?.let { { Text(it) } }
                    )

                    OutlinedTextField(
                        value = state.description,
                        onValueChange = { viewModel.onDescriptionChange(it) },
                        label = { Text("Описание") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    OutlinedTextField(
                        value = state.theme,
                        onValueChange = { viewModel.onThemeChange(it) },
                        label = { Text("Тема") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = state.rules,
                        onValueChange = { viewModel.onRulesChange(it) },
                        label = { Text("Правила") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }

                JamSection(title = "Даты проведения") {
                    DateTimePickerField(
                        label = "Начало регистрации",
                        value = state.registrationStart,
                        onValueChange = { viewModel.onRegistrationStartChange(it) }
                    )
                    DateTimePickerField(
                        label = "Конец регистрации",
                        value = state.registrationEnd,
                        onValueChange = { viewModel.onRegistrationEndChange(it) }
                    )
                    DateTimePickerField(
                        label = "Начало джема",
                        value = state.jamStart,
                        onValueChange = { viewModel.onJamStartChange(it) }
                    )
                    DateTimePickerField(
                        label = "Конец джема",
                        value = state.jamEnd,
                        onValueChange = { viewModel.onJamEndChange(it) }
                    )
                    DateTimePickerField(
                        label = "Начало оценивания",
                        value = state.judgingStart,
                        onValueChange = { viewModel.onJudgingStartChange(it) }
                    )
                    DateTimePickerField(
                        label = "Конец оценивания",
                        value = state.judgingEnd,
                        onValueChange = { viewModel.onJudgingEndChange(it) }
                    )
                }

                JamSection(title = "Настройки команд") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = state.minTeamSize,
                            onValueChange = { viewModel.onMinTeamSizeChange(it) },
                            label = { Text("Мин.") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = state.maxTeamSize,
                            onValueChange = { viewModel.onMaxTeamSizeChange(it) },
                            label = { Text("Макс.") },
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
                        Text("Сохранить изменения")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}