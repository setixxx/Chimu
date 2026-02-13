package software.setixx.chimu.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class JudgeResponse(
    val userId: String,
    val nickname: String,
    val avatarUrl: String? = null,
    val assignedAt: String
)

@Serializable
data class AssignJudgeRequest(
    val judgeUserId: String
)