package software.setixx.chimu.api.dto

data class UserProfileResponse(
    val id: String,
    val email: String,
    val nickname: String,
    val firstName: String?,
    val lastName: String?,
    val specialization: SpecializationResponse?,
    val avatarUrl: String? = null,
    val createdAt: String,
    val skills: List<String>
)