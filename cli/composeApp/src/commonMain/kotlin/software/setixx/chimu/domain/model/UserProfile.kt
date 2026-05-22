package software.setixx.chimu.domain.model

import software.setixx.chimu.api.domain.UserRole

data class UserProfile(
    val id: String,
    val email: String,
    val nickname: String,
    val firstName: String?,
    val lastName: String?,
    val specialization: Specialization?,
    val avatarUrl: String?,
    val createdAt: String,
    val skills: List<Skill>,
    val role: UserRole,
    val bio: String?,
    val githubUrl: String?,
    val telegramUrl: String?,
)

data class PublicUserProfile(
    val id: String,
    val nickname: String,
    val isDeleted: Boolean,
    val role: UserRole? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val specialization: Specialization? = null,
    val avatarUrl: String? = null,
    val createdAt: String? = null,
    val skills: List<String> = emptyList(),
    val bio: String? = null,
    val githubUrl: String? = null,
    val telegramUrl: String? = null,
)

data class ChangePassword(
    val oldPassword: String,
    val newPassword: String
)

data class ChangedPassword(
    val message: String,
    val accessToken: String,
    val refreshToken: String
)