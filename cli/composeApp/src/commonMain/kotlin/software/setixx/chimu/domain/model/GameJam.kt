package software.setixx.chimu.domain.model

import software.setixx.chimu.api.domain.GameJamStatus
import software.setixx.chimu.data.remote.dto.JudgeResponse
import software.setixx.chimu.data.remote.dto.RatingCriteriaResponse

data class GameJam(
    val id: String,
    val name: String,
    val description: String,
    val theme: String,
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
    val bannerUrl: String? = null,
    val createdAt: String
)

data class CreateGameJam(
    val name: String,
    val description: String,
    val theme: String,
    val rules: String,
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
    val description: String,
    val theme: String,
    val rules: String,
    val registrationStart: String,
    val registrationEnd: String,
    val jamStart: String,
    val jamEnd: String,
    val judgingStart: String,
    val judgingEnd: String,
    val status: GameJamStatus,
    val organizerId: String,
    val organizerNickname: String,
    val minTeamSize: Int,
    val maxTeamSize: Int,
    val bannerUrl: String? = null,
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