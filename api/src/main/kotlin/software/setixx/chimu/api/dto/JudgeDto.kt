package software.setixx.chimu.api.dto

import jakarta.validation.constraints.NotBlank

data class JudgeResponse(
    val userId: String,
    val nickname: String,
    val avatarUrl: String?,
    val assignedAt: String
)


data class AssignJudgeRequest(
    @field:NotBlank(message = "Judge user ID is required")
    val judgeUserId: String
)