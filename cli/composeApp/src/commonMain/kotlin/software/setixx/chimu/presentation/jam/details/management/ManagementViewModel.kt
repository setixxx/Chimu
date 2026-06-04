package software.setixx.chimu.presentation.jam.details.management

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import software.setixx.chimu.api.domain.ProjectStatus
import software.setixx.chimu.api.domain.RegistrationStatus
import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.model.AssignJudge
import software.setixx.chimu.domain.model.CreateRatingCriteria
import software.setixx.chimu.domain.model.FileUpload
import software.setixx.chimu.domain.model.UpdateRatingCriteria
import software.setixx.chimu.domain.model.UpdateRegistrationStatus
import software.setixx.chimu.domain.usecase.AssignJudgeUseCase
import software.setixx.chimu.domain.usecase.CreateJamCriteriaUseCase
import software.setixx.chimu.domain.usecase.DeleteJamBannerUseCase
import software.setixx.chimu.domain.usecase.DeleteJamCriteriaUseCase
import software.setixx.chimu.domain.usecase.GetJamCriteriaUseCase
import software.setixx.chimu.domain.usecase.GetJamDetailsUseCase
import software.setixx.chimu.domain.usecase.GetJamJudgesUseCase
import software.setixx.chimu.domain.usecase.GetJamProjectsUseCase
import software.setixx.chimu.domain.usecase.GetJamRegistrationsUseCase
import software.setixx.chimu.domain.usecase.GetJamStatisticsUseCase
import software.setixx.chimu.domain.usecase.GetLeaderboardUseCase
import software.setixx.chimu.domain.usecase.GetUserByNicknameUseCase
import software.setixx.chimu.domain.usecase.PublishJamUseCase
import software.setixx.chimu.domain.usecase.UnassignJudgeUseCase
import software.setixx.chimu.domain.usecase.UpdateJamCriteriaUseCase
import software.setixx.chimu.domain.usecase.UploadJamBannerUseCase
import software.setixx.chimu.domain.usecase.UpdateRegistrationStatusUseCase

/**
 * ViewModel для управления Game Jam (для организаторов).
 * Позволяет назначать судей, управлять критериями оценки и изменять статус регистрации.
 */
class ManagementViewModel(
    private val getJamJudgesUseCase: GetJamJudgesUseCase,
    private val assignJudgeUseCase: AssignJudgeUseCase,
    private val unassignJudgeUseCase: UnassignJudgeUseCase,
    private val getJamRegistrationsUseCase: GetJamRegistrationsUseCase,
    private val updateRegistrationStatusUseCase: UpdateRegistrationStatusUseCase,
    private val getJamCriteriaUseCase: GetJamCriteriaUseCase,
    private val createJamCriteriaUseCase: CreateJamCriteriaUseCase,
    private val updateJamCriteriaUseCase: UpdateJamCriteriaUseCase,
    private val deleteJamCriteriaUseCase: DeleteJamCriteriaUseCase,
    private val publishJamUseCase: PublishJamUseCase,
    private val getJamDetailsUseCase: GetJamDetailsUseCase,
    private val getJamStatisticsUseCase: GetJamStatisticsUseCase,
    private val getLeaderboardUseCase: GetLeaderboardUseCase,
    private val getJamProjectsUseCase: GetJamProjectsUseCase,
    private val uploadJamBannerUseCase: UploadJamBannerUseCase,
    private val deleteJamBannerUseCase: DeleteJamBannerUseCase,
    private val getUserByNicknameUseCase: GetUserByNicknameUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ManagementState())
    val state: StateFlow<ManagementState> = _state.asStateFlow()

    fun load(jamId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val jamResult = getJamDetailsUseCase(jamId)
            if (jamResult is ApiResult.Success) {
                _state.update { it.copy(jam = jamResult.data) }
            }

            val judgesResult = getJamJudgesUseCase(jamId)
            if (judgesResult is ApiResult.Success) {
                _state.update { it.copy(judges = judgesResult.data) }
            }

            val registrationsResult = getJamRegistrationsUseCase(jamId)
            if (registrationsResult is ApiResult.Success) {
                _state.update { it.copy(registrations = registrationsResult.data) }
            }

            val statsResult = getJamStatisticsUseCase(jamId)
            if (statsResult is ApiResult.Success) {
                _state.update { it.copy(statistics = statsResult.data) }
            }

            val leaderboardResult = getLeaderboardUseCase(jamId)
            if (leaderboardResult is ApiResult.Success) {
                _state.update { it.copy(leaderboard = leaderboardResult.data) }
            }

            val projectsResult = getJamProjectsUseCase(jamId, ProjectStatus.SUBMITTED)
            if (projectsResult is ApiResult.Success) {
                _state.update { it.copy(jamProjects = projectsResult.data) }
            }

            when (val criteriaResult = getJamCriteriaUseCase(jamId)) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            criteria = criteriaResult.data,
                            isLoading = false
                        )
                    }
                }
                is ApiResult.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = criteriaResult.message
                        )
                    }
                }
            }
        }
    }

    fun uploadBanner(jamId: String, file: FileUpload) {
        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            when (val result = uploadJamBannerUseCase(jamId, file)) {
                is ApiResult.Success -> {
                    when (val jamResult = getJamDetailsUseCase(jamId)) {
                        is ApiResult.Success -> {
                            _state.update {
                                it.copy(
                                    jam = jamResult.data,
                                    isActionLoading = false,
                                    successMessage = "Баннер загружен"
                                )
                            }
                        }
                        is ApiResult.Error -> {
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = "Ошибка при обновлении данных джема"
                                )
                            }
                        }
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

    fun deleteBanner(jamId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            when (val result = deleteJamBannerUseCase(jamId)) {
                is ApiResult.Success -> {
                    when (val jamResult = getJamDetailsUseCase(jamId)) {
                        is ApiResult.Success -> {
                            _state.update {
                                it.copy(
                                    jam = jamResult.data,
                                    isActionLoading = false,
                                    successMessage = "Баннер удален"
                                )
                            }
                        }
                        is ApiResult.Error -> {
                            _state.update {
                                it.copy(
                                    isActionLoading = false,
                                    errorMessage = "Ошибка при обновлении данных джема"
                                )
                            }
                        }
                    }
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

    fun updateRegistrationStatus(jamId: String, teamId: String, status: RegistrationStatus) {
        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            when (val result = updateRegistrationStatusUseCase(
                jamId, teamId, UpdateRegistrationStatus(status)
            )) {
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

    fun assignJudge(jamId: String) {
        val judgeUserId = _state.value.foundJudge?.id ?: return
        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true, judgeSearchError = null) }
            when (val result = assignJudgeUseCase(jamId, AssignJudge(judgeUserId))) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            judges = it.judges + result.data,
                            isActionLoading = false,
                            successMessage = "Судья назначен",
                            foundJudge = null,
                            judgeSearchQuery = ""
                        )
                    }
                }
                is ApiResult.Error -> {
                    _state.update {
                        it.copy(
                            isActionLoading = false,
                            judgeSearchError = result.message
                        )
                    }
                }
            }
        }
    }

    fun onJudgeSearchQueryChange(query: String) {
        _state.update {
            it.copy(
                judgeSearchQuery = query,
                foundJudge = null,
                judgeSearchError = null
            )
        }
    }

    fun searchJudge() {
        val query = _state.value.judgeSearchQuery.trim()
        if (query.isBlank()) return
        viewModelScope.launch {
            _state.update { it.copy(isSearchingJudge = true, judgeSearchError = null) }
            when (val result = getUserByNicknameUseCase(query)) {
                is ApiResult.Success -> {
                    val user = result.data
                    if (_state.value.judges.any { it.userId == user.id }){
                        _state.update {
                            it.copy(
                                isSearchingJudge = false,
                                judgeSearchError = "Этот пользователь уже назначен судьей"
                            )
                        }
                    } else {
                        _state.update {
                            it.copy(isSearchingJudge = false, foundJudge = user)
                        }
                    }
                }
                is ApiResult.Error -> _state.update {
                    it.copy(isSearchingJudge = false, judgeSearchError = result.message)
                }
            }
        }
    }

    fun unassignJudge(jamId: String, judgeUserId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            when (val result = unassignJudgeUseCase(jamId, judgeUserId)) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            judges = it.judges.filter { it.userId != judgeUserId },
                            isActionLoading = false,
                            successMessage = "Судья снят"
                        )
                    }
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

    fun createCriteria(jamId: String, data: CreateRatingCriteria) {
        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            when (val result = createJamCriteriaUseCase(jamId, data)) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            criteria = it.criteria + result.data,
                            isActionLoading = false,
                            successMessage = "Критерий добавлен"
                        )
                    }
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

    fun updateCriteria(jamId: String, criteriaId: String, data: UpdateRatingCriteria) {
        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            when (val result = updateJamCriteriaUseCase(jamId, criteriaId, data)) {
                is ApiResult.Success -> {
                    _state.update {
                        val updated = it.criteria.map { criteria ->
                            if (criteria.id == criteriaId) result.data else criteria
                        }
                        it.copy(
                            criteria = updated,
                            successMessage = "Критерий обновлен",
                            isActionLoading = false
                        )
                    }
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

    fun deleteCriteria(jamId: String, criteriaId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            when (val result = deleteJamCriteriaUseCase(jamId, criteriaId)) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            criteria = it.criteria.filter { criteria -> criteria.id != criteriaId },
                            successMessage = "Критерий удален",
                            isActionLoading = false
                        )
                    }
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

    fun publishJam(jamId: String) {
        viewModelScope.launch {
            val current = _state.value
            val jamData = current.jam
            if (current.criteria.isEmpty() || current.judges.isEmpty() || jamData?.bannerUrl == null) {
                _state.update {
                    it.copy(
                        errorMessage = "Для публикации добавьте критерии, судей и баннер"
                    )
                }
                return@launch
            }
            _state.update { it.copy(isActionLoading = true) }
            when (val result = publishJamUseCase(jamId)) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            isActionLoading = false,
                            isPublished = true,
                            successMessage = "Джем опубликован"
                        )
                    }
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

    fun clearSuccess() {
        _state.update { it.copy(successMessage = null) }
    }
}
