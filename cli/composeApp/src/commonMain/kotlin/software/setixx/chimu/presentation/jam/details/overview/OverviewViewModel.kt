package software.setixx.chimu.presentation.jam.details.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.model.RegisterTeam
import software.setixx.chimu.domain.usecase.GetJamRegistrationsUseCase
import software.setixx.chimu.domain.usecase.GetTeamDetailsUseCase
import software.setixx.chimu.domain.usecase.GetUserTeamsUseCase
import software.setixx.chimu.domain.usecase.RegisterTeamUseCase
import software.setixx.chimu.domain.usecase.WithdrawTeamUseCase

class OverviewViewModel(
    private val getJamRegistrationsUseCase: GetJamRegistrationsUseCase,
    private val getUserTeamsUseCase: GetUserTeamsUseCase,
    private val registerTeamUseCase: RegisterTeamUseCase,
    private val withdrawTeamUseCase: WithdrawTeamUseCase,
    private val getTeamDetailsUseCase: GetTeamDetailsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(OverviewState())
    val state: StateFlow<OverviewState> = _state.asStateFlow()

    fun load(jamId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            when (val teamsResult = getUserTeamsUseCase()){
                is ApiResult.Success -> {
                    _state.update { it.copy(userTeams = teamsResult.data) }
                }
                is ApiResult.Error -> {
                    _state.update { it.copy(errorMessage = teamsResult.message) }
                }
            }

            when (val result = getJamRegistrationsUseCase(jamId)) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            registrations = result.data,
                            isLoading = false
                        )
                    }
                }
                is ApiResult.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun registerTeam(jamId: String, teamId: String) {
        if (_state.value.hasRegisteredTeam) {
            _state.update { it.copy(errorMessage = "От вас уже зарегистрирована команда.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }

            when (val teamDetailsResult = getTeamDetailsUseCase(teamId)) {
                is ApiResult.Success -> {
                    val membersWithoutSpec = teamDetailsResult.data.members.filter {
                        it.specialization == null
                    }
                    if (membersWithoutSpec.isNotEmpty()) {
                        val names = membersWithoutSpec.joinToString { it.nickname }
                        _state.update {
                            it.copy(
                                isActionLoading = false,
                                errorMessage = "У следующих участников не указана специализация: $names"
                            )
                        }
                        return@launch
                    }
                }
                is ApiResult.Error -> {
                    _state.update {
                        it.copy(
                            isActionLoading = false,
                            errorMessage = teamDetailsResult.message
                        )
                    }
                    return@launch
                }
            }

            when (val result = registerTeamUseCase(jamId, RegisterTeam(teamId))) {
                is ApiResult.Success -> {
                    load(jamId)
                    _state.update { it.copy(isActionLoading = false) }
                }
                is ApiResult.Error -> {
                    _state.update {
                        it.copy(
                            isActionLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun withdrawTeam(jamId: String, teamId: String) {
        val registeredTeam = _state.value.registeredTeam
        if (registeredTeam?.id != teamId) {
            _state.update { it.copy(errorMessage = "Вы можете отменить заявку только своей зарегистрированной команды.") }
            return
        }

        if (!registeredTeam.isLeader) {
            _state.update { it.copy(errorMessage = "Только лидер команды может отменить заявку.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            when (val result = withdrawTeamUseCase(jamId, teamId)) {
                is ApiResult.Success -> {
                    load(jamId)
                    _state.update { it.copy(isActionLoading = false) }
                }
                is ApiResult.Error -> {
                    _state.update {
                        it.copy(
                            isActionLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
}
