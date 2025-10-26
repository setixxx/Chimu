package setixx.software.Chimu.security.dto

data class AuthenticationResponse(
    val accessToken: String,
    val refreshToken: String
)