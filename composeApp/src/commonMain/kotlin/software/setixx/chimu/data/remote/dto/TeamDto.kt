package software.setixx.chimu.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TeamResponse(
    val id: String,
    val name: String,
    val description: String?,
    val leaderId: String,
    val createdAt: String,
    val memberCount: Int,
    val isLeader: Boolean
)

@Serializable
data class TeamDetailsResponse(
    val id: String,
    val name: String,
    val description: String?,
    val leaderId: String,
    val inviteToken: String?,
    val createdAt: String,
    val members: List<TeamMemberResponse>
)
@Serializable
data class TeamMemberResponse(
    val userId: String,
    val nickname: String,
    val avatarUrl: String?,
    val specialization: SpecializationResponse?,
    val joinedAt: String,
    val isLeader: Boolean
)

@Serializable
data class CreateTeamRequest(
    val name: String,
    val description: String? = null
)