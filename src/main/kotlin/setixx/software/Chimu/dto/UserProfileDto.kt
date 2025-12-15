package setixx.software.Chimu.dto

import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class UpdateProfileRequest(
    @field:Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
    val firstName: String? = null,

    @field:Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
    val lastName: String? = null,

    @field:Size(min = 3, max = 50, message = "Nickname must be between 3 and 50 characters")
    @field:Pattern(
        regexp = "^[a-zA-Z0-9_]+$",
        message = "Nickname can only contain letters, numbers, and underscores"
    )
    val nickname: String? = null,

    @field:Size(max = 500, message = "Bio must not exceed 500 characters")
    val bio: String? = null,

    val specializationId: Long? = null,

    @field:Pattern(
        regexp = "^https://github\\.com/[a-zA-Z0-9_-]+/?$",
        message = "Invalid GitHub URL format"
    )
    val githubUrl: String? = null,

    @field:Size(min = 3, max = 32, message = "Telegram username must be between 3 and 32 characters")
    @field:Pattern(
        regexp = "^[a-zA-Z0-9_]+$",
        message = "Telegram username can only contain letters, numbers, and underscores"
    )
    val telegramUsername: String? = null,

    val avatarUrl: String? = null,

    val skillIds: List<Long>? = null
)