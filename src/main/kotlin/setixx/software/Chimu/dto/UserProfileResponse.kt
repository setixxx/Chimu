package setixx.software.Chimu.dto

import setixx.software.Chimu.domain.UserPrimaryRole

data class UserProfileResponse(
    val id: String,
    val email: String,
    val nickname: String,
    val firstName: String?,
    val lastName: String?,
    val primaryRole: UserPrimaryRole?,
    val avatarUrl: String? = null,
    val createdAt: String,
    val skills: List<String>
)