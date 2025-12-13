package setixx.software.Chimu.dto

data class UserProfileResponse(
    val displayName: String,
    val email: String,
    val avatarUrl: String? = null,
    val createdAt: String,
)