package software.setixx.chimu.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class GameJamResponse(
    val id: String,
    val name: String,
    val description: String?,
    val theme: String?,
    val registrationStart: String,
    val registrationEnd: String,
    val jamStart: String,
    val jamEnd: String,
    val judgingStart: String,
    val judgingEnd: String,
    val status: String,
    val organizerId: String,
    val organizerNickname: String,
    val registeredTeamsCount: Int,
    val maxTeamSize: Int,
    val minTeamSize: Int,
    val createdAt: String
)

@Serializable
data class GameJamDetailsResponse(
    val id: String,
    val name: String,
    val description: String?,
    val theme: String?,
    val rules: String?,
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
    val criteria: List<CriteriaResponse>,
    val judges: List<JudgeResponse>,
    val registeredTeamsCount: Int,
    val submittedProjectsCount: Int
)

@Serializable
data class CriteriaResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val maxScore: Int,
    val weight: String,
    val orderIndex: Int
)

@Serializable
data class JudgeResponse(
    val userId: String,
    val nickname: String,
    val avatarUrl: String?,
    val assignedAt: String
)

@Serializable
data class CreateGameJamRequest(
    val name: String,
    val description: String?,
    val theme: String?,
    val rules: String?,
    val registrationStart: String,
    val registrationEnd: String,
    val jamStart: String,
    val jamEnd: String,
    val judgingStart: String,
    val judgingEnd: String,
    val minTeamSize: Int,
    val maxTeamSize: Int
)

@Serializable
data class UpdateGameJamRequest(
    val name: String?,
    val description: String?,
    val theme: String?,
    val rules: String?,
    val registrationStart: String?,
    val registrationEnd: String?,
    val jamStart: String?,
    val jamEnd: String?,
    val judgingStart: String?,
    val judgingEnd: String?,
    val minTeamSize: Int?,
    val maxTeamSize: Int?
)
