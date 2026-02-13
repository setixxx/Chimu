package software.setixx.chimu.presentation.team.create

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.model.CreateTeam
import software.setixx.chimu.domain.usecase.CreateTeamUseCase
import software.setixx.chimu.domain.usecase.GetUserTeamsUseCase

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

            val data = CreateTeam(
                name = _state.value.name.trim(),
                description = _state.value.description.trim().takeIf { it.isNotBlank() }
            )

            when (val result = createTeamUseCase(data)){
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        isCreating = false,
                        isSuccess = true
                    )
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(
                        isCreating = false,
                        errorMessage = result.message
                    )
                }
            }
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