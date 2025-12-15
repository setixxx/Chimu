package setixx.software.Chimu.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateTeamRequest(
    @field:NotBlank(message = "Team name is required")
    @field:Size(min = 3, max = 100, message = "Team name must be between 3 and 100 characters")
    val name: String,

    @field:Size(max = 1000, message = "Description must not exceed 1000 characters")
    val description: String? = null
)

data class UpdateTeamRequest(
    @field:Size(min = 3, max = 100, message = "Team name must be between 3 and 100 characters")
    val name: String? = null,

    @field:Size(max = 1000, message = "Description must not exceed 1000 characters")
    val description: String? = null
)

data class TeamResponse(
    val id: String,
    val name: String,
    val description: String?,
    val leaderId: String,
    val createdAt: String,
    val memberCount: Int,
    val isLeader: Boolean
)

data class TeamDetailsResponse(
    val id: String,
    val name: String,
    val description: String?,
    val leaderId: String,
    val inviteToken: String?,
    val createdAt: String,
    val members: List<TeamMemberResponse>
)

data class TeamMemberResponse(
    val userId: String,
    val nickname: String,
    val avatarUrl: String?,
    val specialization: SpecializationResponse?,
    val joinedAt: String,
    val isLeader: Boolean
)

data class JoinTeamRequest(
    @field:NotBlank(message = "Invite token is required")
    val inviteToken: String
)

data class UpdateMemberSpecializationRequest(
    val specializationId: Long?
)