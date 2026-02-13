package software.setixx.chimu.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val accessToken: String,
    val refreshToken: String
)

@Serializable
data class RegisterResponse(
    val publicId: String
)

@Serializable
data class RefreshTokenRequest(
    val token: String
)

@Serializable
data class TokenResponse(
    val token: String
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
    val skills: List<String>,
    val role: String,
    val specialization: SpecializationResponse? = null,
    val bio: String? = null,
    val githubUrl: String? = null,
    val telegramUrl: String? = null
)

@Serializable
data class ErrorResponse(
    val message: String
)