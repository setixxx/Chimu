package software.setixx.chimu.presentation.jam.details.registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.model.RegisterTeam
import software.setixx.chimu.domain.usecase.GetJamRegistrationsUseCase
import software.setixx.chimu.domain.usecase.GetTeamDetailsUseCase
import software.setixx.chimu.domain.usecase.GetUserTeamsUseCase
import software.setixx.chimu.domain.usecase.RegisterTeamUseCase
import software.setixx.chimu.domain.usecase.WithdrawTeamUseCase

class RegistrationViewModel(
    private val getJamRegistrationsUseCase: GetJamRegistrationsUseCase,
    private val getUserTeamsUseCase: GetUserTeamsUseCase,
    private val registerTeamUseCase: RegisterTeamUseCase,
    private val withdrawTeamUseCase: WithdrawTeamUseCase,
    private val getTeamDetailsUseCase: GetTeamDetailsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RegistrationState())
    val state: StateFlow<RegistrationState> = _state.asStateFlow()

    fun load(jamId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            val teamsResult = getUserTeamsUseCase()
            if (teamsResult is ApiResult.Success) {
                _state.value = _state.value.copy(userTeams = teamsResult.data)
            }

            when (val result = getJamRegistrationsUseCase(jamId)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        registrations = result.data,
                        isLoading = false
                    )
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun registerTeam(jamId: String, teamId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isActionLoading = true)

            when (val teamDetailsResult = getTeamDetailsUseCase(teamId)) {
                is ApiResult.Success -> {
                    val membersWithoutSpec = teamDetailsResult.data.members.filter {
                        it.specialization == null
                    }
                    if (membersWithoutSpec.isNotEmpty()) {
                        val names = membersWithoutSpec.joinToString { it.nickname }
                        _state.value = _state.value.copy(
                            isActionLoading = false,
                            errorMessage = "У следующих участников не указана специализация: $names"
                        )
                        return@launch
                    }
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(
                        isActionLoading = false,
                        errorMessage = teamDetailsResult.message
                    )
                    return@launch
                }
            }

            when (val result = registerTeamUseCase(jamId, RegisterTeam(teamId))) {
                is ApiResult.Success -> {
                    load(jamId)
                    _state.value = _state.value.copy(isActionLoading = false)
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(
                        isActionLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun withdrawTeam(jamId: String, teamId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isActionLoading = true)
            when (val result = withdrawTeamUseCase(jamId, teamId)) {
                is ApiResult.Success -> {
                    load(jamId)
                    _state.value = _state.value.copy(isActionLoading = false)
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(
                        isActionLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}
