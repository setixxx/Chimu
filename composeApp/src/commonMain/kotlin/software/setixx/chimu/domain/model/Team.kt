package software.setixx.chimu.domain.model

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

data class UpdateTeamData(
    val name: String?,
    val description: String?
)