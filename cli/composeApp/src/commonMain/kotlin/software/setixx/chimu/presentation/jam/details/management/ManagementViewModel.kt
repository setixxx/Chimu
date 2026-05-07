package software.setixx.chimu.presentation.jam.details.management

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
import software.setixx.chimu.domain.usecase.GetJamBannerUseCase
import software.setixx.chimu.domain.usecase.GetJamCriteriaUseCase
import software.setixx.chimu.domain.usecase.GetJamJudgesUseCase
import software.setixx.chimu.domain.usecase.GetJamRegistrationsUseCase
import software.setixx.chimu.domain.usecase.PublishJamUseCase
import software.setixx.chimu.domain.usecase.UnassignJudgeUseCase
import software.setixx.chimu.domain.usecase.UpdateJamCriteriaUseCase
import software.setixx.chimu.domain.usecase.UploadJamBannerUseCase
import software.setixx.chimu.domain.usecase.UpdateRegistrationStatusUseCase

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
    private val getJamBannerUseCase: GetJamBannerUseCase,
    private val uploadJamBannerUseCase: UploadJamBannerUseCase,
    private val deleteJamBannerUseCase: DeleteJamBannerUseCase
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

            val registrationsResult = getJamRegistrationsUseCase(jamId)
            if (registrationsResult is ApiResult.Success) {
                _state.value = _state.value.copy(registrations = registrationsResult.data)
            }

            val bannerResult = getJamBannerUseCase(jamId)
            _state.value = _state.value.copy(hasBanner = bannerResult is ApiResult.Success)

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

    fun updateRegistrationStatus(jamId: String, teamId: String, status: RegistrationStatus) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isActionLoading = true)
            when (val result = updateRegistrationStatusUseCase(
                jamId,
                teamId,
                UpdateRegistrationStatus(status)
            )) {
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

    fun publishJam(jamId: String) {
        viewModelScope.launch {
            val current = _state.value
            if (current.criteria.isEmpty() || current.judges.isEmpty() || !current.hasBanner) {
                _state.value = current.copy(
                    errorMessage = "Для публикации добавьте критерии, судей и баннер"
                )
                return@launch
            }

            _state.value = current.copy(isActionLoading = true)
            when (val result = publishJamUseCase(jamId)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        isActionLoading = false,
                        isPublished = true,
                        successMessage = "Джем опубликован"
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

    fun uploadBanner(jamId: String, file: FileUpload) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isActionLoading = true)
            when (val result = uploadJamBannerUseCase(jamId, file)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        hasBanner = true,
                        isActionLoading = false,
                        successMessage = "Баннер загружен"
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

    fun deleteBanner(jamId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isActionLoading = true)
            when (val result = deleteJamBannerUseCase(jamId)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        hasBanner = false,
                        isActionLoading = false,
                        successMessage = "Баннер удален"
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
