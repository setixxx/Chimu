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
    val accessToken: String,
    val refreshToken: String
)

@Serializable
data class ErrorResponse(
    val message: String
)