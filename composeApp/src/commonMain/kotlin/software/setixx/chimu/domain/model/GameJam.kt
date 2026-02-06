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