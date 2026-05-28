package software.setixx.chimu.presentation.jam.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import software.setixx.chimu.api.domain.UserRole
import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.model.CreateGameJam
import software.setixx.chimu.domain.usecase.CreateJamUseCase
import software.setixx.chimu.domain.usecase.GetCurrentUserUseCase

/**
 * ViewModel для создания нового Game Jam.
 * Обрабатывает ввод параметров мероприятия и проверяет права пользователя на создание.
 */
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
                    _state.update { it.copy(userRole = role) }
                    if (role != UserRole.ADMIN && role != UserRole.ORGANIZER) {
                        _state.update {
                            it.copy(
                                errorMessage = "У вас нет прав для создания Game Jam"
                            )
                        }
                    }
                }
                is ApiResult.Error -> {
                    _state.update { it.copy(errorMessage = result.message) }
                }
            }
        }
    }

    fun onNameChange(name: String) {
        _state.update { it.copy(name = name, nameError = null) }
    }

    fun onDescriptionChange(description: String) {
        _state.update { it.copy(description = description) }
    }

    fun onThemeChange(theme: String) {
        _state.update { it.copy(theme = theme) }
    }

    fun onRulesChange(rules: String) {
        _state.update { it.copy(rules = rules) }
    }

    fun onRegistrationStartChange(date: String) {
        _state.update { it.copy(registrationStart = date, dateError = null) }
    }

    fun onRegistrationEndChange(date: String) {
        _state.update { it.copy(registrationEnd = date, dateError = null) }
    }

    fun onJamStartChange(date: String) {
        _state.update { it.copy(jamStart = date, dateError = null) }
    }

    fun onJamEndChange(date: String) {
        _state.update { it.copy(jamEnd = date, dateError = null) }
    }

    fun onJudgingStartChange(date: String) {
        _state.update { it.copy(judgingStart = date, dateError = null) }
    }

    fun onJudgingEndChange(date: String) {
        _state.update { it.copy(judgingEnd = date, dateError = null) }
    }

    fun onMinTeamSizeChange(size: String) {
        if (size.isEmpty() || size.all { it.isDigit() }) {
            _state.update { it.copy(minTeamSize = size, teamSizeError = null) }
        }
    }

    fun onMaxTeamSizeChange(size: String) {
        if (size.isEmpty() || size.all { it.isDigit() }) {
            _state.update { it.copy(maxTeamSize = size, teamSizeError = null) }
        }
    }

    fun createJam() {
        if (!validateInputs()) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val currentState = _state.value
            val createGameJam = CreateGameJam(
                name = currentState.name,
                description = currentState.description,
                theme = currentState.theme,
                rules = currentState.rules,
                registrationStart = currentState.registrationStart,
                registrationEnd = currentState.registrationEnd,
                jamStart = currentState.jamStart,
                jamEnd = currentState.jamEnd,
                judgingStart = currentState.judgingStart,
                judgingEnd = currentState.judgingEnd,
                minTeamSize = currentState.minTeamSize.toIntOrNull() ?: 1,
                maxTeamSize = currentState.maxTeamSize.toIntOrNull() ?: 5
            )

            when (val result = createJamUseCase(createGameJam)) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = true,
                            createdJamId = result.data.id
                        )
                    }
                }
                is ApiResult.Error -> {
                    _state.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        if (_state.value.name.isBlank()) {
            _state.update { it.copy(nameError = "Название обязательно") }
            isValid = false
        }

        if (_state.value.description.isBlank()) {
            _state.update { it.copy(nameError = "Описание обязательно") }
            isValid = false
        }

        if (_state.value.theme.isBlank()) {
            _state.update { it.copy(nameError = "Тема обязательна") }
            isValid = false
        }

        if (_state.value.rules.isBlank()) {
            _state.update { it.copy(nameError = "Правила обязательны") }
            isValid = false
        }

        if (_state.value.registrationStart.isBlank() || _state.value.registrationEnd.isBlank() ||
            _state.value.jamStart.isBlank() || _state.value.jamEnd.isBlank() ||
            _state.value.judgingStart.isBlank() || _state.value.judgingEnd.isBlank()) {
            _state.update { it.copy(dateError = "Все даты должны быть заполнены") }
            isValid = false
        }

        val minSize = _state.value.minTeamSize.toIntOrNull()
        val maxSize = _state.value.maxTeamSize.toIntOrNull()

        if (minSize == null || maxSize == null || minSize > maxSize || minSize < 1) {
            _state.update { it.copy(teamSizeError = "Некорректный размер команды") }
            isValid = false
        }

        return isValid
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
}
