package software.setixx.chimu.presentation.team.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.model.CreateTeam
import software.setixx.chimu.domain.usecase.CreateTeamUseCase
import software.setixx.chimu.domain.usecase.GetUserTeamsUseCase

/**
 * ViewModel для создания новой команды.
 * Обрабатывает ввод названия и описания команды, а также валидацию данных.
 */
class CreateTeamViewModel(
    private val createTeamUseCase: CreateTeamUseCase,
    private val getUserTeamsUseCase: GetUserTeamsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CreateTeamState())
    val state: StateFlow<CreateTeamState> = _state.asStateFlow()


    fun updateName(name: String) {
        _state.update {
            it.copy(
                name = name,
                nameError = null
            )
        }
    }

    fun updateDescription(description: String) {
        _state.update { it.copy(description = description) }
    }

    fun createTeam() {
        if (!validateInputs()) return

        viewModelScope.launch {
            _state.update { it.copy(isCreating = true) }

            val currentState = _state.value
            val data = CreateTeam(
                name = currentState.name.trim(),
                description = currentState.description.trim().takeIf { it.isNotBlank() }
            )

            when (val result = createTeamUseCase(data)){
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            isCreating = false,
                            isSuccess = true,
                            createdTeamId = result.data.id
                        )
                    }
                }
                is ApiResult.Error -> {
                    _state.update {
                        it.copy(
                            isCreating = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    private fun validateInputs(): Boolean {
        val name = _state.value.name.trim()

        if (name.isBlank()) {
            _state.update {
                it.copy(
                    nameError = "Название не может быть пустым"
                )
            }
            return false
        }

        if (name.length < 3) {
            _state.update {
                it.copy(
                    nameError = "Название должно содержать минимум 3 символа"
                )
            }
            return false
        }

        if (name.length > 100) {
            _state.update {
                it.copy(
                    nameError = "Название не может превышать 100 символов"
                )
            }
            return false
        }

        if (!name.matches(Regex("^[\\p{L}0-9]+( [\\p{L}0-9]+)*$"))) {
            _state.update {
                it.copy(
                    nameError = "Название может содержать только буквы, цифры и пробелы"
                )
            }
            return false
        }

        return true
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
}