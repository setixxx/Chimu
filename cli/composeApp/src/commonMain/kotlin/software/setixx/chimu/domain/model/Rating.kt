package software.setixx.chimu.domain.model

data class ProjectRating(
    val projectId: String,
    val criteriaRatings: List<ProjectCriteriaRating>
)

data class ProjectCriteriaRating(
    val criteriaId: Long,
    val criteriaName: String,
    val maxScore: Int,
    val weight: String,
    val averageScore: String,
    val judgeRatings: List<ProjectJudgeRating>
)

data class ProjectJudgeRating(
    val judgeNickname: String,
    val score: String,
    val comment: String? = null
)

data class RateProject(
    val criteriaId: Long,
    val score: Int,
    val comment: String? = null
)

data class Rating(
    val id: Long,
    val projectId: String,
    val judgeId: String,
    val judgeNickname: String,
    val criteriaId: Long,
    val criteriaName: String,
    val score: String,
    val maxScore: Int,
    val comment: String? = null,
    val createdAt: String,
    val updatedAt: String
)

data class UpdateRating(
    val score: Int,
    val comment: String? = null
)

data class MyRating(
    val id: Long,
    val criteriaId: Long,
    val criteriaName: String,
    val score: String,
    val maxScore: Int,
    val comment: String? = null,
    val updatedAt: String
)

data class JudgeProgress(
    val jamId: String,
    val jamName: String,
    val totalProjects: Int,
    val ratedProjects: Int,
    val missingProjects: List<MissingProjectInfo>
)

data class MissingProjectInfo(
    val projectId: String,
    val projectTitle: String,
    val teamName: String? = null,
    val missingCriteria: List<String>
)