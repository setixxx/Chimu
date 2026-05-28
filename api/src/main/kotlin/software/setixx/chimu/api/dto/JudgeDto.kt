package software.setixx.chimu.api.dto

import jakarta.validation.constraints.NotBlank

/**
 * Ответ с информацией о судье.
 */
data class JudgeResponse(
    val userId: String,
    val nickname: String,
    val avatarUrl: String?,
    val assignedAt: String
)


/**
 * Запрос на назначение пользователя судьей.
 */
data class AssignJudgeRequest(
    @field:NotBlank(message = "Judge user ID is required")
    val judgeUserId: String
)