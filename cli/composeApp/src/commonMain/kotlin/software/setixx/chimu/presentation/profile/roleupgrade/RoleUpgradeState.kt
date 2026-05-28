package software.setixx.chimu.presentation.profile.roleupgrade

import software.setixx.chimu.api.domain.RoleRequestStatus
import software.setixx.chimu.api.domain.UserRole
import software.setixx.chimu.domain.model.RoleUpgrade

/**
 * Состояние экрана управления ролями.
 * Содержит список запросов, выбранную роль для заявки и фильтры для администратора.
 */
data class RoleUpgradeState(
    val requests: List<RoleUpgrade> = emptyList(),
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showCreateForm: Boolean = false,
    val selectedRole: UserRole? = null,
    val userMessage: String = "",
    val adminFilter: AdminFilter = AdminFilter.PENDING,
    val expandedRequestId: String? = null,
    val adminMessage: String = ""
) {
    val hasPendingRequest: Boolean
        get() = requests.any { it.status == RoleRequestStatus.PENDING }

    val filteredRequests: List<RoleUpgrade>
        get() = when (adminFilter) {
            AdminFilter.ALL -> requests
            AdminFilter.PENDING -> requests.filter { it.status == RoleRequestStatus.PENDING }
        }
}

/**
 * Фильтр для администратора при просмотре заявок на смену роли.
 */
enum class AdminFilter { ALL, PENDING }