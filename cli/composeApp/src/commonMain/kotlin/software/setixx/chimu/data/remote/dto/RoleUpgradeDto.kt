package software.setixx.chimu.data.remote.dto

import kotlinx.serialization.Serializable
import software.setixx.chimu.api.domain.RoleRequestStatus
import software.setixx.chimu.api.domain.UserRole

@Serializable
data class CreateRoleUpgradeRequest(
    val requestedRole: UserRole,
    val userMessage: String? = null
)

@Serializable
data class ReviewRoleUpgradeRequest(
    val status: RoleRequestStatus,
    val adminMessage: String? = null
)

@Serializable
data class RoleUpgradeResponse(
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