package software.setixx.chimu.presentation.jam.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.model.FileUpload
import software.setixx.chimu.domain.model.UpdateGameJam
import software.setixx.chimu.domain.usecase.DeleteJamBannerUseCase
import software.setixx.chimu.domain.usecase.GetJamDetailsUseCase
import software.setixx.chimu.domain.usecase.UpdateJamUseCase
import software.setixx.chimu.domain.usecase.UploadJamBannerUseCase

/**
 * ViewModel для редактирования существующего Game Jam.
 * Позволяет организаторам изменять сроки проведения, правила и другие параметры мероприятия.
 */
class EditJamViewModel(
    private val getJamDetailsUseCase: GetJamDetailsUseCase,
    private val updateJamUseCase: UpdateJamUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(EditJamState())
    val state: StateFlow<EditJamState> = _state.asStateFlow()

    fun loadJam(jamId: String) {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = getJamDetailsUseCase(jamId)) {
                is ApiResult.Success -> {
                    val jam = result.data
                    _state.update {
                        it.copy(
                            jam = jam,
                            name = jam.name,
                            description = jam.description,
                            theme = jam.theme,
                            rules = jam.rules,
                            registrationStart = jam.registrationStart,
                            registrationEnd = jam.registrationEnd,
                            jamStart = jam.jamStart,
                            jamEnd = jam.jamEnd,
                            judgingStart = jam.judgingStart,
                            judgingEnd = jam.judgingEnd,
                            minTeamSize = jam.minTeamSize.toString(),
                            maxTeamSize = jam.maxTeamSize.toString(),
                            isLoading = false
                        )
                    }
                }
                is ApiResult.Error -> {
                    _state.update {
                        it.copy(
                            errorMessage = result.message,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    fun onNameChange(name: String) = _state.update { it.copy(name = name, nameError = null) }
    fun onDescriptionChange(description: String) = _state.update { it.copy(description = description) }
    fun onThemeChange(theme: String) = _state.update { it.copy(theme = theme) }
    fun onRulesChange(rules: String) = _state.update { it.copy(rules = rules) }
    fun onRegistrationStartChange(date: String) = _state.update { it.copy(registrationStart = date, dateError = null) }
    fun onRegistrationEndChange(date: String) = _state.update { it.copy(registrationEnd = date, dateError = null) }
    fun onJamStartChange(date: String) = _state.update { it.copy(jamStart = date, dateError = null) }
    fun onJamEndChange(date: String) = _state.update { it.copy(jamEnd = date, dateError = null) }
    fun onJudgingStartChange(date: String) = _state.update { it.copy(judgingStart = date, dateError = null) }
    fun onJudgingEndChange(date: String) = _state.update { it.copy(judgingEnd = date, dateError = null) }

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

    fun updateJam() {
        val jamId = _state.value.jam?.id ?: return
        if (!validateInputs()) return

        viewModelScope.launch {
            _state.update { it.copy(isUpdating = true) }
            
            val currentState = _state.value
            val updateData = UpdateGameJam(
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
                minTeamSize = currentState.minTeamSize.toIntOrNull(),
                maxTeamSize = currentState.maxTeamSize.toIntOrNull()
            )

            when (val result = updateJamUseCase(jamId, updateData)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(isUpdating = false, isSuccess = true) }
                }
                is ApiResult.Error -> {
                    _state.update { it.copy(isUpdating = false, errorMessage = result.message) }
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