package software.setixx.chimu.presentation.team

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import software.setixx.chimu.domain.model.CreateTeamData
import software.setixx.chimu.domain.usecase.CreateTeamUseCase
import software.setixx.chimu.domain.usecase.GetUserTeamsUseCase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTeamScreen(
    onBack: () -> Unit,
    onTeamCreated: () -> Unit,
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
        if (state.isSuccess) {
            onTeamCreated()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Создать команду") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Назад")
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
                        "Основная информация",
                        style = MaterialTheme.typography.titleMedium
                    )

                    OutlinedTextField(
                        value = state.name,
                        onValueChange = { viewModel.updateName(it) },
                        label = { Text("Название команды *") },
                        enabled = !state.isCreating,
                        modifier = Modifier.fillMaxWidth(),
                        isError = state.nameError != null,
                        supportingText = state.nameError?.let { { Text(it) } },
                        leadingIcon = { Icon(Icons.Default.Group, null) },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = state.description,
                        onValueChange = { viewModel.updateDescription(it) },
                        label = { Text("Описание") },
                        enabled = !state.isCreating,
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        leadingIcon = { Icon(Icons.Default.Description, null) }
                    )
                }
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
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
                            "Важная информация",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }

                    Text(
                        "• Вы автоматически станете лидером команды",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        "• Вы можете создать до 10 команд",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        "• Название должно быть уникальным",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        "• После создания вы получите токен приглашения",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.createTeam() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isCreating
            ) {
                if (state.isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Создать команду")
            }
        }
    }
}

data class CreateTeamState(
    val name: String = "",
    val description: String = "",
    val nameError: String? = null,
    val isCreating: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

class CreateTeamViewModel(
    private val createTeamUseCase: CreateTeamUseCase,
    private val getUserTeamsUseCase: GetUserTeamsUseCase
) : androidx.lifecycle.ViewModel() {

    private val _state = kotlinx.coroutines.flow.MutableStateFlow(CreateTeamState())
    val state: kotlinx.coroutines.flow.StateFlow<CreateTeamState> = _state.asStateFlow()

    fun updateName(name: String) {
        _state.value = _state.value.copy(
            name = name,
            nameError = null
        )
    }

    fun updateDescription(description: String) {
        _state.value = _state.value.copy(description = description)
    }

    fun createTeam() {
        if (!validateInputs()) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isCreating = true)

            val data = CreateTeamData(
                name = _state.value.name.trim(),
                description = _state.value.description.trim().takeIf { it.isNotBlank() }
            )

            createTeamUseCase(data).fold(
                onSuccess = {
                    _state.value = _state.value.copy(
                        isCreating = false,
                        isSuccess = true
                    )
                },
                onFailure = { error ->
                    _state.value = _state.value.copy(
                        isCreating = false,
                        errorMessage = error.message ?: "Ошибка при создании команды"
                    )
                }
            )
        }
    }

    private fun validateInputs(): Boolean {
        val name = _state.value.name.trim()

        if (name.isBlank()) {
            _state.value = _state.value.copy(
                nameError = "Название не может быть пустым"
            )
            return false
        }

        if (name.length < 3) {
            _state.value = _state.value.copy(
                nameError = "Название должно содержать минимум 3 символа"
            )
            return false
        }

        if (name.length > 100) {
            _state.value = _state.value.copy(
                nameError = "Название не может превышать 100 символов"
            )
            return false
        }

        if (!name.matches(Regex("^[a-zA-Z0-9]+( [a-zA-Z0-9]+)*$"))) {
            _state.value = _state.value.copy(
                nameError = "Название может содержать только буквы, цифры и пробелы"
            )
            return false
        }

        return true
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}