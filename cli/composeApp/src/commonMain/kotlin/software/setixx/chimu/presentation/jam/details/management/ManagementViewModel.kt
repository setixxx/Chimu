package software.setixx.chimu.presentation.jam.details.management

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.model.AssignJudge
import software.setixx.chimu.domain.model.CreateRatingCriteria
import software.setixx.chimu.domain.model.UpdateRatingCriteria
import software.setixx.chimu.domain.usecase.AssignJudgeUseCase
import software.setixx.chimu.domain.usecase.CreateJamCriteriaUseCase
import software.setixx.chimu.domain.usecase.DeleteJamCriteriaUseCase
import software.setixx.chimu.domain.usecase.GetJamCriteriaUseCase
import software.setixx.chimu.domain.usecase.GetJamJudgesUseCase
import software.setixx.chimu.domain.usecase.UnassignJudgeUseCase
import software.setixx.chimu.domain.usecase.UpdateJamCriteriaUseCase

class ManagementViewModel(
    private val getJamJudgesUseCase: GetJamJudgesUseCase,
    private val assignJudgeUseCase: AssignJudgeUseCase,
    private val unassignJudgeUseCase: UnassignJudgeUseCase,
    private val getJamCriteriaUseCase: GetJamCriteriaUseCase,
    private val createJamCriteriaUseCase: CreateJamCriteriaUseCase,
    private val updateJamCriteriaUseCase: UpdateJamCriteriaUseCase,
    private val deleteJamCriteriaUseCase: DeleteJamCriteriaUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ManagementState())
    val state: StateFlow<ManagementState> = _state.asStateFlow()

    fun load(jamId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            val judgesResult = getJamJudgesUseCase(jamId)
            if (judgesResult is ApiResult.Success) {
                _state.value = _state.value.copy(judges = judgesResult.data)
            }

            when (val criteriaResult = getJamCriteriaUseCase(jamId)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        criteria = criteriaResult.data,
                        isLoading = false
                    )
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = criteriaResult.message
                    )
                }
            }
        }
    }

    fun assignJudge(jamId: String, judgeUserId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isActionLoading = true)
            when (val result = assignJudgeUseCase(jamId, AssignJudge(judgeUserId))) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        judges = _state.value.judges + result.data,
                        isActionLoading = false,
                        successMessage = "Судья назначен"
                    )
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

    fun unassignJudge(jamId: String, judgeUserId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isActionLoading = true)
            when (val result = unassignJudgeUseCase(jamId, judgeUserId)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        judges = _state.value.judges.filter { it.userId != judgeUserId },
                        isActionLoading = false,
                        successMessage = "Судья снят"
                    )
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

    fun createCriteria(jamId: String, data: CreateRatingCriteria) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isActionLoading = true)
            when (val result = createJamCriteriaUseCase(jamId, data)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        criteria = _state.value.criteria + result.data,
                        isActionLoading = false,
                        successMessage = "Критерий добавлен"
                    )
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

    fun updateCriteria(jamId: String, criteriaId: Long, data: UpdateRatingCriteria) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isActionLoading = true)
            when (val result = updateJamCriteriaUseCase(jamId, criteriaId, data)) {
                is ApiResult.Success -> {
                    val updated = _state.value.criteria.map {
                        if (it.id == criteriaId) result.data else it
                    }
                    _state.value = _state.value.copy(
                        criteria = updated,
                        isActionLoading = false
                    )
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

    fun deleteCriteria(jamId: String, criteriaId: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isActionLoading = true)
            when (val result = deleteJamCriteriaUseCase(jamId, criteriaId)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        criteria = _state.value.criteria.filter { it.id != criteriaId },
                        isActionLoading = false
                    )
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

    fun clearSuccess() {
        _state.value = _state.value.copy(successMessage = null)
    }
}