package software.setixx.chimu.api.dto

import software.setixx.chimu.api.domain.UserRole

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