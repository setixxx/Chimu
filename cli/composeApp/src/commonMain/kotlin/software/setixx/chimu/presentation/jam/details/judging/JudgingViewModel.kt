package software.setixx.chimu.presentation.jam.details.judging

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import software.setixx.chimu.api.domain.ProjectStatus
import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.model.Project
import software.setixx.chimu.domain.model.RateProject
import software.setixx.chimu.domain.model.UpdateRating
import software.setixx.chimu.domain.usecase.DeleteProjectRatingUseCase
import software.setixx.chimu.domain.usecase.GetJamProjectsUseCase
import software.setixx.chimu.domain.usecase.GetJudgeProgressUseCase
import software.setixx.chimu.domain.usecase.GetMyRatingsUseCase
import software.setixx.chimu.domain.usecase.GetUserProjectsUseCase
import software.setixx.chimu.domain.usecase.RateProjectUseCase
import software.setixx.chimu.domain.usecase.UpdateProjectRatingUseCase

class JudgingViewModel(
    private val getJamProjectsUseCase: GetJamProjectsUseCase,
    private val getJudgeProgressUseCase: GetJudgeProgressUseCase,
    private val getMyRatingsUseCase: GetMyRatingsUseCase,
    private val rateProjectUseCase: RateProjectUseCase,
    private val deleteProjectRatingUseCase: DeleteProjectRatingUseCase,
    private val updateProjectRatingUseCase: UpdateProjectRatingUseCase,
    private val getUserProjectsUseCase: GetUserProjectsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(JudgingState())
    val state: StateFlow<JudgingState> = _state.asStateFlow()

    fun loadAsJudge(jamId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            when (val projectsResult = getJamProjectsUseCase(jamId, ProjectStatus.SUBMITTED)) {
                is ApiResult.Success -> _state.value = _state.value.copy(projects = projectsResult.data)
                is ApiResult.Error -> _state.value = _state.value.copy(errorMessage = projectsResult.message)
            }

            when (val progressResult = getJudgeProgressUseCase(jamId)) {
                is ApiResult.Success -> _state.value = _state.value.copy(judgeProgress = progressResult.data)
                is ApiResult.Error -> _state.value = _state.value.copy(errorMessage = progressResult.message)
            }

            _state.value = _state.value.copy(isLoading = false)
        }
    }

    fun loadAsParticipant(jamId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            when (val result = getUserProjectsUseCase()) {
                is ApiResult.Success -> {
                    val userProject = result.data.find { it.jamId == jamId }
                    _state.value = _state.value.copy(userProject = userProject)
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(errorMessage = result.message)
                }
            }

            _state.value = _state.value.copy(isLoading = false)
        }
    }

    fun selectProject(project: Project) {
        _state.value = _state.value.copy(
            selectedProject = project,
            myRatings = emptyList()
        )
        loadMyRatings(project.id)
    }

    fun clearSelectedProject() {
        _state.value = _state.value.copy(
            selectedProject = null,
            myRatings = emptyList()
        )
    }

    private fun loadMyRatings(projectId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRatingLoading = true)
            when (val result = getMyRatingsUseCase(projectId)) {
                is ApiResult.Success -> _state.value = _state.value.copy(
                    myRatings = result.data,
                    isRatingLoading = false
                )
                is ApiResult.Error -> _state.value = _state.value.copy(
                    isRatingLoading = false,
                    errorMessage = result.message
                )
            }
        }
    }

    fun rateProject(projectId: String, criteriaId: Long, score: Int, comment: String?) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isActionLoading = true)
            when (val result = rateProjectUseCase(
                projectId,
                RateProject(criteriaId = criteriaId, score = score, comment = comment?.takeIf { it.isNotBlank() })
            )) {
                is ApiResult.Success -> {
                    loadMyRatings(projectId)
                    _state.value = _state.value.copy(
                        isActionLoading = false,
                        successMessage = "Оценка сохранена"
                    )
                }
                is ApiResult.Error -> _state.value = _state.value.copy(
                    isActionLoading = false,
                    errorMessage = result.message
                )
            }
        }
    }

    fun updateRating(ratingId: Long, projectId: String, score: Int, comment: String?) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isActionLoading = true)
            when (val result = updateProjectRatingUseCase(
                ratingId.toString(),
                UpdateRating(score = score, comment = comment?.takeIf { it.isNotBlank() })
            )) {
                is ApiResult.Success -> {
                    loadMyRatings(projectId)
                    _state.value = _state.value.copy(
                        isActionLoading = false,
                        successMessage = "Оценка обновлена"
                    )
                }
                is ApiResult.Error -> _state.value = _state.value.copy(
                    isActionLoading = false,
                    errorMessage = result.message
                )
            }
        }
    }

    fun deleteRating(ratingId: Long, projectId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isActionLoading = true)
            when (val result = deleteProjectRatingUseCase(ratingId.toString())) {
                is ApiResult.Success -> {
                    loadMyRatings(projectId)
                    _state.value = _state.value.copy(
                        isActionLoading = false,
                        successMessage = "Оценка удалена"
                    )
                }
                is ApiResult.Error -> _state.value = _state.value.copy(
                    isActionLoading = false,
                    errorMessage = result.message
                )
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