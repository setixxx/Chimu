package software.setixx.chimu.presentation.jam.create

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
import software.setixx.chimu.presentation.jam.create.components.JamSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateJamScreen(
    onBack: () -> Unit,
    onJamCreated: () -> Unit,
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
        if (state.isSuccess) {
            onJamCreated()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Создать Game Jam") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Назад")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (state.userRole != "ADMIN" && state.userRole != "ORGANIZER" && state.userRole != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("У вас нет прав для создания Game Jam", style = MaterialTheme.typography.titleLarge)
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
                    if (state.dateError != null) {
                        Text(
                            text = state.dateError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    OutlinedTextField(
                        value = state.registrationStart,
                        onValueChange = { viewModel.onRegistrationStartChange(it) },
                        label = { Text("Начало регистрации (YYYY-MM-DD) *") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = state.registrationEnd,
                        onValueChange = { viewModel.onRegistrationEndChange(it) },
                        label = { Text("Конец регистрации (YYYY-MM-DD) *") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = state.jamStart,
                        onValueChange = { viewModel.onJamStartChange(it) },
                        label = { Text("Начало джема (YYYY-MM-DD) *") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = state.jamEnd,
                        onValueChange = { viewModel.onJamEndChange(it) },
                        label = { Text("Конец джема (YYYY-MM-DD) *") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = state.judgingStart,
                        onValueChange = { viewModel.onJudgingStartChange(it) },
                        label = { Text("Начало оценивания (YYYY-MM-DD) *") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = state.judgingEnd,
                        onValueChange = { viewModel.onJudgingEndChange(it) },
                        label = { Text("Конец оценивания (YYYY-MM-DD) *") },
                        modifier = Modifier.fillMaxWidth()
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
                            label = { Text("Мин. участников") },
                            modifier = Modifier.weight(1f),
                            isError = state.teamSizeError != null
                        )

                        OutlinedTextField(
                            value = state.maxTeamSize,
                            onValueChange = { viewModel.onMaxTeamSizeChange(it) },
                            label = { Text("Макс. участников") },
                            modifier = Modifier.weight(1f),
                            isError = state.teamSizeError != null
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
                        .height(56.dp),
                    enabled = !state.isLoading
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Создать джем")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}