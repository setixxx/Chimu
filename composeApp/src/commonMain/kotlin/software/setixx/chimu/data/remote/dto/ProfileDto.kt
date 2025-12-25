package software.setixx.chimu.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfileRequest(
    val firstName: String? = null,
    val lastName: String? = null,
    val nickname: String? = null,
    val bio: String? = null,
    val specializationId: Long? = null,
    val githubUrl: String? = null,
    val telegramUsername: String? = null,
    val avatarUrl: String? = null,
    val skillIds: List<Long>? = null
)

