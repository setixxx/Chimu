package software.setixx.chimu.presentation.jam.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.model.CreateGameJam
import software.setixx.chimu.domain.usecase.CreateJamUseCase
import software.setixx.chimu.domain.usecase.GetCurrentUserUseCase

class CreateJamViewModel(
    private val createJamUseCase: CreateJamUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CreateJamState())
    val state: StateFlow<CreateJamState> = _state.asStateFlow()

    init {
        checkUserRole()
    }

    private fun checkUserRole() {
        viewModelScope.launch {
            when (val result = getCurrentUserUseCase()) {
                is ApiResult.Success -> {
                    val role = result.data.role
                    _state.value = _state.value.copy(userRole = role)
                    if (role != "ADMIN" && role != "ORGANIZER") {
                        _state.value = _state.value.copy(
                            errorMessage = "У вас нет прав для создания Game Jam"
                        )
                    }
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(errorMessage = result.message)
                }
            }
        }
    }

    fun onNameChange(name: String) {
        _state.value = _state.value.copy(name = name, nameError = null)
    }

    fun onDescriptionChange(description: String) {
        _state.value = _state.value.copy(description = description)
    }

    fun onThemeChange(theme: String) {
        _state.value = _state.value.copy(theme = theme)
    }

    fun onRulesChange(rules: String) {
        _state.value = _state.value.copy(rules = rules)
    }

    fun onRegistrationStartChange(date: String) {
        _state.value = _state.value.copy(registrationStart = date, dateError = null)
    }

    fun onRegistrationEndChange(date: String) {
        _state.value = _state.value.copy(registrationEnd = date, dateError = null)
    }

    fun onJamStartChange(date: String) {
        _state.value = _state.value.copy(jamStart = date, dateError = null)
    }

    fun onJamEndChange(date: String) {
        _state.value = _state.value.copy(jamEnd = date, dateError = null)
    }

    fun onJudgingStartChange(date: String) {
        _state.value = _state.value.copy(judgingStart = date, dateError = null)
    }

    fun onJudgingEndChange(date: String) {
        _state.value = _state.value.copy(judgingEnd = date, dateError = null)
    }

    fun onMinTeamSizeChange(size: String) {
        if (size.isEmpty() || size.all { it.isDigit() }) {
            _state.value = _state.value.copy(minTeamSize = size, teamSizeError = null)
        }
    }

    fun onMaxTeamSizeChange(size: String) {
        if (size.isEmpty() || size.all { it.isDigit() }) {
            _state.value = _state.value.copy(maxTeamSize = size, teamSizeError = null)
        }
    }

    fun createJam() {
        if (!validateInputs()) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            val createGameJam = CreateGameJam(
                name = _state.value.name,
                description = _state.value.description.takeIf { it.isNotBlank() },
                theme = _state.value.theme.takeIf { it.isNotBlank() },
                rules = _state.value.rules.takeIf { it.isNotBlank() },
                registrationStart = _state.value.registrationStart,
                registrationEnd = _state.value.registrationEnd,
                jamStart = _state.value.jamStart,
                jamEnd = _state.value.jamEnd,
                judgingStart = _state.value.judgingStart,
                judgingEnd = _state.value.judgingEnd,
                minTeamSize = _state.value.minTeamSize.toIntOrNull() ?: 1,
                maxTeamSize = _state.value.maxTeamSize.toIntOrNull() ?: 5
            )

            when (val result = createJamUseCase(createGameJam)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(isLoading = false, isSuccess = true)
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true
        
        if (_state.value.name.isBlank()) {
            _state.value = _state.value.copy(nameError = "Название обязательно")
            isValid = false
        }

        if (_state.value.registrationStart.isBlank() || _state.value.registrationEnd.isBlank() ||
            _state.value.jamStart.isBlank() || _state.value.jamEnd.isBlank() ||
            _state.value.judgingStart.isBlank() || _state.value.judgingEnd.isBlank()) {
            _state.value = _state.value.copy(dateError = "Все даты должны быть заполнены")
            isValid = false
        }

        val minSize = _state.value.minTeamSize.toIntOrNull()
        val maxSize = _state.value.maxTeamSize.toIntOrNull()

        if (minSize == null || maxSize == null || minSize > maxSize || minSize < 1) {
            _state.value = _state.value.copy(teamSizeError = "Некорректный размер команды")
            isValid = false
        }

        return isValid
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}