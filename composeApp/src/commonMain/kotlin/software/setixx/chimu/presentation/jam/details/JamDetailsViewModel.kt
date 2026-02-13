package software.setixx.chimu.presentation.jam.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import software.setixx.chimu.domain.model.*
import software.setixx.chimu.domain.usecase.*

class JamDetailsViewModel(
    private val getJamDetailsUseCase: GetJamDetailsUseCase,
    private val deleteJamUseCase: DeleteJamUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getJamRegistrationsUseCase: GetJamRegistrationsUseCase,
    private val registerTeamUseCase: RegisterTeamUseCase,
    private val withdrawTeamUseCase: WithdrawTeamUseCase,
    private val updateRegistrationStatusUseCase: UpdateRegistrationStatusUseCase,
    private val getUserTeamsUseCase: GetUserTeamsUseCase,
    private val getTeamDetailsUseCase: GetTeamDetailsUseCase,
    private val getJamJudgesUseCase: GetJamJudgesUseCase,
    private val assignJudgeUseCase: AssignJudgeUseCase,
    private val unassignJudgeUseCase: UnassignJudgeUseCase,
    private val getJamCriteriaUseCase: GetJamCriteriaUseCase,
    private val createJamCriteriaUseCase: CreateJamCriteriaUseCase,
    private val updateJamCriteriaUseCase: UpdateJamCriteriaUseCase,
    private val deleteJamCriteriaUseCase: DeleteJamCriteriaUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(JamDetailsState())
    val state: StateFlow<JamDetailsState> = _state.asStateFlow()

    fun loadJamDetails(jamId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            val userResult = getCurrentUserUseCase()
            if (userResult is ApiResult.Success) {
                _state.value = _state.value.copy(
                    userRole = userResult.data.role,
                    userId = userResult.data.id
                )
                
                if (userResult.data.role == "PARTICIPANT") {
                    loadUserTeams()
                }
            }

            loadRegistrations(jamId)
            loadJudges(jamId)
            loadCriteria(jamId)

            when (val result = getJamDetailsUseCase(jamId)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        jamDetails = result.data,
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

    private suspend fun loadUserTeams() {
        when (val result = getUserTeamsUseCase()) {
            is ApiResult.Success -> {
                _state.value = _state.value.copy(userTeams = result.data.filter { it.isLeader })
            }
            else -> {}
        }
    }

    private suspend fun loadRegistrations(jamId: String) {
        when (val result = getJamRegistrationsUseCase(jamId)) {
            is ApiResult.Success -> {
                _state.value = _state.value.copy(registrations = result.data)
            }
            else -> {}
        }
    }

    private suspend fun loadJudges(jamId: String) {
        when (val result = getJamJudgesUseCase(jamId)) {
            is ApiResult.Success -> {
                _state.value = _state.value.copy(judges = result.data)
            }
            else -> {}
        }
    }

    private suspend fun loadCriteria(jamId: String) {
        when (val result = getJamCriteriaUseCase(jamId)) {
            is ApiResult.Success -> {
                _state.value = _state.value.copy(criteria = result.data)
            }
            else -> {}
        }
    }

    fun assignJudge(jamId: String, judgeUserId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isActionLoading = true)
            when (val result = assignJudgeUseCase(jamId, AssignJudge(judgeUserId))) {
                is ApiResult.Success -> {
                    loadJudges(jamId)
                    _state.value = _state.value.copy(isActionLoading = false)
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(isActionLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun unassignJudge(jamId: String, judgeUserId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isActionLoading = true)
            when (val result = unassignJudgeUseCase(jamId, judgeUserId)) {
                is ApiResult.Success -> {
                    loadJudges(jamId)
                    _state.value = _state.value.copy(isActionLoading = false)
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(isActionLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun createCriteria(jamId: String, data: CreateRatingCriteria) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isActionLoading = true)
            when (val result = createJamCriteriaUseCase(jamId, data)) {
                is ApiResult.Success -> {
                    loadCriteria(jamId)
                    _state.value = _state.value.copy(isActionLoading = false)
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(isActionLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun updateCriteria(jamId: String, criteriaId: Long, data: UpdateRatingCriteria) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isActionLoading = true)
            when (val result = updateJamCriteriaUseCase(jamId, criteriaId, data)) {
                is ApiResult.Success -> {
                    loadCriteria(jamId)
                    _state.value = _state.value.copy(isActionLoading = false)
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(isActionLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun deleteCriteria(jamId: String, criteriaId: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isActionLoading = true)
            when (val result = deleteJamCriteriaUseCase(jamId, criteriaId)) {
                is ApiResult.Success -> {
                    loadCriteria(jamId)
                    _state.value = _state.value.copy(isActionLoading = false)
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(isActionLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun registerTeam(jamId: String, teamId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isActionLoading = true)
            
            when (val teamDetailsResult = getTeamDetailsUseCase(teamId)) {
                is ApiResult.Success -> {
                    val membersWithoutSpec = teamDetailsResult.data.members.filter { it.specialization == null }
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
                    _state.value = _state.value.copy(isActionLoading = false, errorMessage = teamDetailsResult.message)
                    return@launch
                }
            }

            when (val result = registerTeamUseCase(jamId, RegisterTeam(teamId))) {
                is ApiResult.Success -> {
                    loadRegistrations(jamId)
                    _state.value = _state.value.copy(isActionLoading = false)
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(isActionLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun withdrawTeam(jamId: String, teamId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isActionLoading = true)
            when (val result = withdrawTeamUseCase(jamId, teamId)) {
                is ApiResult.Success -> {
                    loadRegistrations(jamId)
                    _state.value = _state.value.copy(isActionLoading = false)
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(isActionLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun updateRegistrationStatus(jamId: String, teamId: String, status: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isActionLoading = true)
            when (val result = updateRegistrationStatusUseCase(jamId, teamId, UpdateRegistrationStatus(status))) {
                is ApiResult.Success -> {
                    loadRegistrations(jamId)
                    _state.value = _state.value.copy(isActionLoading = false)
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(isActionLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun deleteJam(jamId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isDeleting = true)
            when (val result = deleteJamUseCase(jamId)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(isDeleting = false, isDeleted = true)
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(isDeleting = false, errorMessage = result.message)
                }
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}