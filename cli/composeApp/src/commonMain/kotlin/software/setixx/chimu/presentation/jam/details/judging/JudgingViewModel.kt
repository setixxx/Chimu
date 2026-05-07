package software.setixx.chimu.presentation.jam.details.judging

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import software.setixx.chimu.api.domain.ProjectStatus
import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.usecase.GetJamProjectsUseCase
import software.setixx.chimu.domain.usecase.GetJudgeProgressUseCase

class JudgingViewModel(
    private val getJamProjectsUseCase: GetJamProjectsUseCase,
    private val getJudgeProgressUseCase: GetJudgeProgressUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(JudgingState())
    val state: StateFlow<JudgingState> = _state.asStateFlow()

    fun load(jamId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            when (val projectsResult = getJamProjectsUseCase(jamId, ProjectStatus.SUBMITTED)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(projects = projectsResult.data)
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(errorMessage = projectsResult.message)
                }
            }

            when (val progressResult = getJudgeProgressUseCase(jamId)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        judgeProgress = progressResult.data,
                        isLoading = false
                    )
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = progressResult.message
                    )
                }
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}