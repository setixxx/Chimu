package setixx.software.Chimu.dto

data class ChangePasswordResponse(
    val message: String,
    val accessToken: String,
    val refreshToken: String
)