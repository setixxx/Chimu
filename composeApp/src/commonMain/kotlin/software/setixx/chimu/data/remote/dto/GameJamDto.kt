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