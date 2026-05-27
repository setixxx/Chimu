package software.setixx.chimu.presentation.jam.details.judging

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import software.setixx.chimu.api.domain.ProjectStatus
import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.model.Project
import software.setixx.chimu.domain.model.RateProject
import software.setixx.chimu.domain.model.UpdateRating
import software.setixx.chimu.domain.usecase.DeleteProjectRatingUseCase
import software.setixx.chimu.domain.usecase.GetJamDetailsUseCase
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
    private val getUserProjectsUseCase: GetUserProjectsUseCase,
    private val getJamDetailsUseCase: GetJamDetailsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(JudgingState())
    val state: StateFlow<JudgingState> = _state.asStateFlow()

    fun loadAsJudge(jamId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            when (val projectsResult = getJamProjectsUseCase(jamId, ProjectStatus.SUBMITTED)) {
                is ApiResult.Success -> _state.update { it.copy(projects = projectsResult.data) }
                is ApiResult.Error -> _state.update { it.copy(errorMessage = projectsResult.message) }
            }

            when (val progressResult = getJudgeProgressUseCase(jamId)) {
                is ApiResult.Success -> _state.update { it.copy(judgeProgress = progressResult.data) }
                is ApiResult.Error -> _state.update { it.copy(errorMessage = progressResult.message) }
            }

            _state.update { it.copy(isLoading = false) }
        }
    }

    fun loadAsParticipant(jamId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            when (val result = getUserProjectsUseCase()) {
                is ApiResult.Success -> {
                    val userProject = result.data.find { it.jamId == jamId }
                    _state.update { it.copy(userProject = userProject) }
                }
                is ApiResult.Error -> {
                    _state.update { it.copy(errorMessage = result.message) }
                }
            }

            _state.update { it.copy(isLoading = false) }
        }
    }

    fun selectProject(project: Project) {
        _state.update {
            it.copy(
                selectedProject = project,
                myRatings = emptyList()
            )
        }
        loadMyRatings(project.id)
    }

    fun clearSelectedProject() {
        _state.update {
            it.copy(
                selectedProject = null,
                myRatings = emptyList()
            )
        }
    }

    fun loadProjectRating(jamId: String, projectId: String) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    selectedProject = null,
                    jamCriteria = emptyList(),
                    myRatings = emptyList()
                )
            }

            when (val jamResult = getJamDetailsUseCase(jamId)) {
                is ApiResult.Success -> _state.update { it.copy(jamCriteria = jamResult.data.criteria) }
                is ApiResult.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = jamResult.message
                        )
                    }
                    return@launch
                }
            }

            when (val projectsResult = getJamProjectsUseCase(jamId, ProjectStatus.SUBMITTED)) {
                is ApiResult.Success -> {
                    val project = projectsResult.data.find { it.id == projectId }
                    if (project == null) {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Проект не найден"
                            )
                        }
                    } else {
                        _state.update {
                            it.copy(
                                selectedProject = project,
                                isLoading = false
                            )
                        }
                        loadMyRatings(projectId)
                    }
                }
                is ApiResult.Error -> _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = projectsResult.message
                    )
                }
            }
        }
    }

    private fun loadMyRatings(projectId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isRatingLoading = true) }
            when (val result = getMyRatingsUseCase(projectId)) {
                is ApiResult.Success -> _state.update {
                    it.copy(
                        myRatings = result.data,
                        isRatingLoading = false
                    )
                }
                is ApiResult.Error -> _state.update {
                    it.copy(
                        isRatingLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun rateProject(projectId: String, criteriaId: String, score: Int, comment: String?) {
        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            when (val result = rateProjectUseCase(
                projectId,
                RateProject(criteriaId = criteriaId, score = score, comment = comment?.takeIf { it.isNotBlank() })
            )) {
                is ApiResult.Success -> {
                    loadMyRatings(projectId)
                    _state.update {
                        it.copy(
                            isActionLoading = false,
                            successMessage = "Оценка сохранена"
                        )
                    }
                }
                is ApiResult.Error -> _state.update {
                    it.copy(
                        isActionLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun updateRating(ratingId: String, projectId: String, score: Int, comment: String?) {
        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            when (val result = updateProjectRatingUseCase(
                ratingId,
                UpdateRating(score = score, comment = comment?.takeIf { it.isNotBlank() })
            )) {
                is ApiResult.Success -> {
                    loadMyRatings(projectId)
                    _state.update {
                        it.copy(
                            isActionLoading = false,
                            successMessage = "Оценка обновлена"
                        )
                    }
                }
                is ApiResult.Error -> _state.update {
                    it.copy(
                        isActionLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun deleteRating(ratingId: String, projectId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            when (val result = deleteProjectRatingUseCase(ratingId)) {
                is ApiResult.Success -> {
                    loadMyRatings(projectId)
                    _state.update {
                        it.copy(
                            isActionLoading = false,
                            successMessage = "Оценка удалена"
                        )
                    }
                }
                is ApiResult.Error -> _state.update {
                    it.copy(
                        isActionLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    fun clearSuccess() {
        _state.update { it.copy(successMessage = null) }
    }
}
