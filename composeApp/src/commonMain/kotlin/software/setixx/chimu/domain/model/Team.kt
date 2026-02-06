package software.setixx.chimu.domain.model

data class Team(
    val id: String,
    val name: String,
    val description: String?,
    val memberCount: Int,
    val isLeader: Boolean,
    val createdAt: String
)

data class TeamDetails(
    val id: String,
    val name: String,
    val description: String?,
    val leaderId: String,
    val inviteToken: String?,
    val createdAt: String,
    val members: List<TeamMember>
)

data class TeamMember(
    val userId: String,
    val nickname: String,
    val avatarUrl: String?,
    val specialization: Specialization?,
    val joinedAt: String,
    val isLeader: Boolean
)

data class UpdateTeam(
    val name: String?,
    val description: String?
)

data class CreateTeam(
    val name: String,
    val description: String? = null
)