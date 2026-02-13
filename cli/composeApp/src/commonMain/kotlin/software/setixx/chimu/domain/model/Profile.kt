package software.setixx.chimu.domain.model

data class Specialization(
    val id: Long,
    val name: String,
    val description: String?
)

data class Skill(
    val id: Long,
    val name: String
)

data class ProfileUpdate(
    val firstName: String? = null,
    val lastName: String? = null,
    val nickname: String? = null,
    val bio: String? = null,
    val specializationId: Long? = null,
    val githubUrl: String? = null,
    val telegramUsername: String? = null,
    val skillIds: List<Long>? = null
)

