package setixx.software.Chimu.security.dto

data class UserProfileResponse(
    val displayName: String,
    val email: String,
    val avatarUrl: String? = null,
    val createdAt: String,
)