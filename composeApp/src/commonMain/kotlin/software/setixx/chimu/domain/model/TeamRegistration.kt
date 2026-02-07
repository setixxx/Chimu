package software.setixx.chimu.domain.model

data class RegisterTeam(
    val teamId: String
)

data class Registration(
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

data class UpdateRegistrationStatus(
    val status: String
)

enum class RegistrationStatus {
    PENDING,
    APPROVED,
    REJECTED,
    WITHDRAWN
}