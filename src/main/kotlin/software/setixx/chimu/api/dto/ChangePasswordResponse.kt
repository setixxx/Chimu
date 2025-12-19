package software.setixx.chimu.api.dto

data class ChangePasswordResponse(
    val message: String,
    val accessToken: String,
    val refreshToken: String
)