package setixx.software.Chimu.dto

data class AuthenticationResponse(
    val accessToken: String,
    val refreshToken: String
)