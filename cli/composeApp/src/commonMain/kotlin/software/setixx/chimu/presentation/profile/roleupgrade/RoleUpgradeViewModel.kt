package software.setixx.chimu.presentation.profile.roleupgrade

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import software.setixx.chimu.api.domain.RoleRequestStatus
import software.setixx.chimu.api.domain.UserRole
import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.model.CreateRoleUpgrade
import software.setixx.chimu.domain.model.ReviewRoleUpgrade
import software.setixx.chimu.domain.usecase.CancelRoleUpgradeUseCase
import software.setixx.chimu.domain.usecase.CreateRoleUpgradeUseCase
import software.setixx.chimu.domain.usecase.GetAllRoleUpgradesUseCase
import software.setixx.chimu.domain.usecase.GetUserRoleUpgradesUseCase
import software.setixx.chimu.domain.usecase.ReviewRoleUpgradeUseCase

class RoleUpgradeViewModel(
    private val getUserRoleUpgradesUseCase: GetUserRoleUpgradesUseCase,
    private val createRoleUpgradeUseCase: CreateRoleUpgradeUseCase,
    private val cancelRoleUpgradeUseCase: CancelRoleUpgradeUseCase,
    private val getAllRoleUpgradesUseCase: GetAllRoleUpgradesUseCase,
    private val reviewRoleUpgradeUseCase: ReviewRoleUpgradeUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RoleUpgradeState())
    val state: StateFlow<RoleUpgradeState> = _state.asStateFlow()

    fun loadForUser() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = getUserRoleUpgradesUseCase()) {
                is ApiResult.Success -> _state.update { it.copy(requests = result.data, isLoading = false) }
                is ApiResult.Error -> _state.update { it.copy(isLoading = false, errorMessage = result.message) }
            }
        }
    }

    fun loadForAdmin() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = getAllRoleUpgradesUseCase()) {
                is ApiResult.Success -> _state.update { it.copy(requests = result.data, isLoading = false) }
                is ApiResult.Error -> _state.update { it.copy(isLoading = false, errorMessage = result.message) }
            }
        }
    }

    fun showCreateForm() = _state.update { it.copy(showCreateForm = true, selectedRole = null, userMessage = "") }

    fun hideCreateForm() = _state.update { it.copy(showCreateForm = false) }

    fun selectRole(role: UserRole) = _state.update { it.copy(selectedRole = role) }

    fun updateUserMessage(message: String) = _state.update { it.copy(userMessage = message) }

    fun submitRequest() {
        val role = _state.value.selectedRole ?: return
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true) }
            val data = CreateRoleUpgrade(
                requestedRole = role,
                userMessage = _state.value.userMessage.takeIf { it.isNotBlank() }
            )
            when (val result = createRoleUpgradeUseCase(data)) {
                is ApiResult.Success -> _state.update {
                    it.copy(
                        isSubmitting = false,
                        showCreateForm = false,
                        requests = listOf(result.data) + it.requests,
                        successMessage = "Заявка отправлена"
                    )
                }
                is ApiResult.Error -> _state.update { it.copy(isSubmitting = false, errorMessage = result.message) }
            }
        }
    }

    fun cancelRequest(requestId: String) {
        viewModelScope.launch {
            when (val result = cancelRoleUpgradeUseCase(requestId)) {
                is ApiResult.Success -> _state.update { state ->
                    state.copy(requests = state.requests.map { if (it.id == requestId) result.data else it })
                }
                is ApiResult.Error -> _state.update { it.copy(errorMessage = result.message) }
            }
        }
    }

    fun setAdminFilter(filter: AdminFilter) = _state.update { it.copy(adminFilter = filter) }

    fun toggleExpandRequest(requestId: String) = _state.update {
        it.copy(
            expandedRequestId = if (it.expandedRequestId == requestId) null else requestId,
            adminMessage = ""
        )
    }

    fun updateAdminMessage(message: String) = _state.update { it.copy(adminMessage = message) }

    fun approveRequest(requestId: String) = review(requestId, RoleRequestStatus.APPROVED)

    fun rejectRequest(requestId: String) = review(requestId, RoleRequestStatus.REJECTED)

    private fun review(requestId: String, status: RoleRequestStatus) {
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true) }
            val data = ReviewRoleUpgrade(
                status = status,
                adminMessage = _state.value.adminMessage.takeIf { it.isNotBlank() }
            )
            when (val result = reviewRoleUpgradeUseCase(requestId, data)) {
                is ApiResult.Success -> _state.update { state ->
                    state.copy(
                        isSubmitting = false,
                        expandedRequestId = null,
                        requests = state.requests.map { if (it.id == requestId) result.data else it },
                        successMessage = if (status == RoleRequestStatus.APPROVED) "Заявка одобрена" else "Заявка отклонена"
                    )
                }
                is ApiResult.Error -> _state.update { it.copy(isSubmitting = false, errorMessage = result.message) }
            }
        }
    }


    fun clearError() = _state.update { it.copy(errorMessage = null) }
    fun clearSuccess() = _state.update { it.copy(successMessage = null) }
}