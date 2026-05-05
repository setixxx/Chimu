package software.setixx.chimu.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfileRequest(
    val firstName: String? = null,
    val lastName: String? = null,
    val nickname: String? = null,
    val bio: String? = null,
    val specializationId: Long? = null,
    val githubUrl: String? = null,
    val telegramUsername: String? = null,
    val avatarUrl: String? = null,
    val skillIds: List<Long>? = null
)

@Serializable
data class UserProfileResponse(
    val id: String,
    val email: String,
    val nickname: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val avatarUrl: String? = null,
    val createdAt: String,
    val skills: List<SkillResponse>,
    val role: String,
    val specialization: SpecializationResponse? = null,
    val bio: String? = null,
    val githubUrl: String? = null,
    val telegramUrl: String? = null
)

@Serializable
data class PublicUserProfileResponse(
    val id: String,
    val nickname: String,
    val isDeleted: Boolean,
    val firstName: String? = null,
    val lastName: String? = null,
    val specialization: SpecializationResponse? = null,
    val avatarUrl: String? = null,
    val createdAt: String? = null,
    val skills: List<SkillResponse>? = null,
    val bio: String? = null,
    val githubUrl: String? = null,
    val telegramUrl: String? = null,
)

@Serializable
data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)

@Serializable
data class ChangePasswordResponse(
    val message: String,
    val accessToken: String,
    val refreshToken: String
)