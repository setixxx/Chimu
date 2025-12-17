package software.setixx.chimu.domain.model

data class AuthTokens(
    val accessToken: String,
    val refreshToken: String
)