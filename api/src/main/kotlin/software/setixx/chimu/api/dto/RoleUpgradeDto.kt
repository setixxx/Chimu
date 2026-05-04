package software.setixx.chimu.api.dto

import jakarta.validation.constraints.NotNull
import software.setixx.chimu.api.domain.RoleRequestStatus
import software.setixx.chimu.api.domain.UserRole

data class CreateRoleUpgradeRequest(
    @field:NotNull(message = "Requested role is required")
    val requestedRole: UserRole,
    val userMessage: String? = null
)

data class ReviewRoleUpgradeRequest(
    @field:NotNull(message = "Status is required")
    val status: RoleRequestStatus,
    val adminMessage: String? = null
)

data class RoleUpgradeRequestResponse(
    val id: String,
    val userId: String,
    val userNickname: String,
    val requestedRole: String,
    val status: RoleRequestStatus,
    val userMessage: String?,
    val adminMessage: String?,
    val reviewedBy: String?,
    val createdAt: String,
    val updatedAt: String
)