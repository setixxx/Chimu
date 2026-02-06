package software.setixx.chimu.api.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import software.setixx.chimu.api.domain.UserRole

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

data class UserProfileResponse(
    val id: String,
    val email: String,
    val nickname: String,
    val firstName: String?,
    val lastName: String?,
    val specialization: SpecializationResponse?,
    val avatarUrl: String?,
    val createdAt: String,
    val skills: List<String>?,
    val role: UserRole,
    val bio: String?,
    val githubUrl: String?,
    val telegramUrl: String?,
)

data class ChangePasswordRequest(
    @field:NotBlank(message = "Old password is required")
    val oldPassword: String,

    @field:NotBlank(message = "New password is required")
    @field:Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    @field:Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
        message = "Password must contain at least one uppercase letter, one lowercase letter, and one digit"
    )
    val newPassword: String
)

data class ChangePasswordResponse(
    val message: String,
    val accessToken: String,
    val refreshToken: String
)