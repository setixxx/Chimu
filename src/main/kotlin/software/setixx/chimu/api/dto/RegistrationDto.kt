package software.setixx.chimu.api.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import software.setixx.chimu.api.domain.RegistrationStatus

data class RegisterTeamRequest(
    @field:NotBlank(message = "Team ID is required")
    val teamId: String
)

data class RegistrationResponse(
    val id: Long,
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

data class UpdateRegistrationStatusRequest(
    @field:NotNull(message = "Status is required")
    val status: RegistrationStatus
)