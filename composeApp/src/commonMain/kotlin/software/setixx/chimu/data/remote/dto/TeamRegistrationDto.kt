package software.setixx.chimu.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterTeamRequest(
    val teamId: String
)

@Serializable
data class RegistrationResponse(
    val id: Long,
    val jamId: String,
    val jamName: String,
    val teamId: String,
    val teamName: String,
    val status: String,
    val registeredAt: String,
    val registeredBy: String,
    val registeredByNickname: String,
    val updatedAt: String
)

@Serializable
data class UpdateRegistrationStatusRequest(
    val status: String
)

enum class RegistrationStatus {
    PENDING,
    APPROVED,
    REJECTED,
    WITHDRAWN
}