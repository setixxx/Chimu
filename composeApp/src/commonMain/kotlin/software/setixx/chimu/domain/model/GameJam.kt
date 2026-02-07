package software.setixx.chimu.domain.model

import software.setixx.chimu.data.remote.dto.JudgeResponse
import software.setixx.chimu.data.remote.dto.RatingCriteriaResponse

data class GameJam(
    val id: String,
    val name: String,
    val description: String? = null,
    val theme: String? = null,
    val registrationStart: String,
    val registrationEnd: String,
    val jamStart: String,
    val jamEnd: String,
    val judgingStart: String,
    val judgingEnd: String,
    val status: GameJamStatus,
    val organizerId: String,
    val organizerNickname: String,
    val registeredTeamsCount: Int,
    val maxTeamSize: Int,
    val minTeamSize: Int,
    val createdAt: String
)

data class CreateGameJam(
    val name: String,
    val description: String? = null,
    val theme: String? = null,
    val rules: String? = null,
    val registrationStart: String,
    val registrationEnd: String,
    val jamStart: String,
    val jamEnd: String,
    val judgingStart: String,
    val judgingEnd: String,
    val minTeamSize: Int,
    val maxTeamSize: Int
)

data class GameJamDetails(
    val id: String,
    val name: String,
    val description: String? = null,
    val theme: String? = null,
    val rules: String? = null,
    val registrationStart: String,
    val registrationEnd: String,
    val jamStart: String,
    val jamEnd: String,
    val judgingStart: String,
    val judgingEnd: String,
    val status: String,
    val organizerId: String,
    val organizerNickname: String,
    val minTeamSize: Int,
    val maxTeamSize: Int,
    val createdAt: String,
    val updatedAt: String,
    val criteria: List<RatingCriteriaResponse>,
    val judges: List<JudgeResponse>,
    val registeredTeamsCount: Int,
    val submittedProjectsCount: Int
)

data class UpdateGameJam(
    val name: String? = null,
    val description: String? = null,
    val theme: String? = null,
    val rules: String? = null,
    val registrationStart: String? = null,
    val registrationEnd: String? = null,
    val jamStart: String? = null,
    val jamEnd: String? = null,
    val judgingStart: String? = null,
    val judgingEnd: String? = null,
    val minTeamSize: Int? = null,
    val maxTeamSize: Int? = null
)

enum class GameJamStatus {
    REGISTRATION_OPEN,
    REGISTRATION_CLOSED,
    IN_PROGRESS,
    JUDGING,
    COMPLETED,
    CANCELLED
}