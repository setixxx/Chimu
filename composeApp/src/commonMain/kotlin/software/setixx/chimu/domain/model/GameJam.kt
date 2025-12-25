package software.setixx.chimu.domain.model

data class GameJam(
    val id: String,
    val name: String,
    val description: String?,
    val theme: String?,
    val status: GameJamStatus,
    val organizerNickname: String,
    val registeredTeamsCount: Int,
    val registrationEnd: String,
    val jamEnd: String,
    val daysRemaining: Int?
)

enum class GameJamStatus {
    REGISTRATION_OPEN,
    REGISTRATION_CLOSED,
    IN_PROGRESS,
    JUDGING,
    COMPLETED,
    CANCELLED
}

data class Team(
    val id: String,
    val name: String,
    val description: String?,
    val memberCount: Int,
    val isLeader: Boolean,
    val createdAt: String
)

data class Project(
    val id: String,
    val jamName: String,
    val teamName: String?,
    val title: String,
    val status: ProjectStatus,
    val submittedAt: String?
)

enum class ProjectStatus {
    DRAFT,
    SUBMITTED,
    UNDER_REVIEW,
    PUBLISHED,
    DISQUALIFIED
}