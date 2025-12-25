package software.setixx.chimu.domain.model

data class User(
    val id: String,
    val email: String,
    val nickname: String,
    val firstName: String?,
    val lastName: String?,
    val avatarUrl: String?,
    val createdAt: String,
    val role: String
)