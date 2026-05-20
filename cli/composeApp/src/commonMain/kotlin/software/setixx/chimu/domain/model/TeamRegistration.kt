package software.setixx.chimu.domain.model

import software.setixx.chimu.api.domain.RegistrationStatus

data class RegisterTeam(
    val teamId: String
)

data class Registration(
    val id: String,
    val jamId: String,
    val jamName: String,
    val teamId: String,
    val teamName: String,
    val status: RegistrationStatus,
    val registeredAt: String,
    val registeredBy: String,
    val registeredByNickname: String,
    val updatedAt: String
)

data class UpdateRegistrationStatus(
    val status: RegistrationStatus
)