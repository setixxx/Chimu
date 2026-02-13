package software.setixx.chimu.domain.model

data class Judge(
    val userId: String,
    val nickname: String,
    val avatarUrl: String?,
    val assignedAt: String
)

data class AssignJudge(
    val judgeUserId: String
)