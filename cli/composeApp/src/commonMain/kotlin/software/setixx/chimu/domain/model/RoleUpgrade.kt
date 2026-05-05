package software.setixx.chimu.domain.model

import software.setixx.chimu.api.domain.RoleRequestStatus
import software.setixx.chimu.api.domain.UserRole

data class CreateRoleUpgrade(
    val requestedRole: UserRole,
    val userMessage: String? = null
)

data class ReviewRoleUpgrade(
    val status: RoleRequestStatus,
    val adminMessage: String? = null
)

data class RoleUpgrade(
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