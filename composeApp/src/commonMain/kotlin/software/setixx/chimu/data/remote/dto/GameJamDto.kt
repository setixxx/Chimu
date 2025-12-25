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
data class ProjectResponse(
    val id: String,
    val jamId: String,
    val jamName: String,
    val teamId: String?,
    val teamName: String?,
    val title: String,
    val description: String?,
    val gameUrl: String?,
    val repositoryUrl: String?,
    val status: String,
    val submittedAt: String?,
    val createdAt: String,
    val updatedAt: String
)