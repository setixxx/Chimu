package software.setixx.chimu.api.dto

data class AuthenticationResponse(
    val accessToken: String,
    val refreshToken: String
)