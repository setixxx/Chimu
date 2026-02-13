package software.setixx.chimu.presentation.jam.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.model.UpdateGameJam
import software.setixx.chimu.domain.usecase.GetJamDetailsUseCase
import software.setixx.chimu.domain.usecase.UpdateJamUseCase

class EditJamViewModel(
    private val getJamDetailsUseCase: GetJamDetailsUseCase,
    private val updateJamUseCase: UpdateJamUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(EditJamState())
    val state: StateFlow<EditJamState> = _state.asStateFlow()

    fun loadJam(jamId: String) {
        _state.value = _state.value.copy(jamId = jamId, isLoading = true)
        viewModelScope.launch {
            when (val result = getJamDetailsUseCase(jamId)) {
                is ApiResult.Success -> {
                    val jam = result.data
                    _state.value = _state.value.copy(
                        name = jam.name,
                        description = jam.description ?: "",
                        theme = jam.theme ?: "",
                        rules = jam.rules ?: "",
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
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(
                        errorMessage = result.message,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onNameChange(name: String) = _state.value.let { _state.value = it.copy(name = name, nameError = null) }
    fun onDescriptionChange(description: String) = _state.value.let { _state.value = it.copy(description = description) }
    fun onThemeChange(theme: String) = _state.value.let { _state.value = it.copy(theme = theme) }
    fun onRulesChange(rules: String) = _state.value.let { _state.value = it.copy(rules = rules) }
    fun onRegistrationStartChange(date: String) = _state.value.let { _state.value = it.copy(registrationStart = date, dateError = null) }
    fun onRegistrationEndChange(date: String) = _state.value.let { _state.value = it.copy(registrationEnd = date, dateError = null) }
    fun onJamStartChange(date: String) = _state.value.let { _state.value = it.copy(jamStart = date, dateError = null) }
    fun onJamEndChange(date: String) = _state.value.let { _state.value = it.copy(jamEnd = date, dateError = null) }
    fun onJudgingStartChange(date: String) = _state.value.let { _state.value = it.copy(judgingStart = date, dateError = null) }
    fun onJudgingEndChange(date: String) = _state.value.let { _state.value = it.copy(judgingEnd = date, dateError = null) }

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

    fun updateJam() {
        if (!validateInputs()) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isUpdating = true)
            
            val updateData = UpdateGameJam(
                name = _state.value.name,
                description = _state.value.description,
                theme = _state.value.theme,
                rules = _state.value.rules,
                registrationStart = _state.value.registrationStart,
                registrationEnd = _state.value.registrationEnd,
                jamStart = _state.value.jamStart,
                jamEnd = _state.value.jamEnd,
                judgingStart = _state.value.judgingStart,
                judgingEnd = _state.value.judgingEnd,
                minTeamSize = _state.value.minTeamSize.toIntOrNull(),
                maxTeamSize = _state.value.maxTeamSize.toIntOrNull()
            )

            when (val result = updateJamUseCase(_state.value.jamId, updateData)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(isUpdating = false, isSuccess = true)
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(isUpdating = false, errorMessage = result.message)
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