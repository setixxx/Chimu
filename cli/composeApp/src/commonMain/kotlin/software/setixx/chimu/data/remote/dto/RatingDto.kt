package software.setixx.chimu.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProjectRatingResponse(
    val projectId: String,
    val criteriaRatings: List<ProjectCriteriaRatingResponse>
)

@Serializable
data class ProjectCriteriaRatingResponse(
    val criteriaId: Long,
    val criteriaName: String,
    val maxScore: Int,
    val weight: String,
    val averageScore: String,
    val judgeRatings: List<ProjectJudgeRatingResponse>
)

@Serializable
data class ProjectJudgeRatingResponse(
    val judgeNickname: String,
    val score: String,
    val comment: String? = null
)

@Serializable
data class RateProjectRequest(
    val criteriaId: Long,
    val score: Int,
    val comment: String? = null
)

@Serializable
data class RatingResponse(
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

@Serializable
data class UpdateRatingRequest(
    val score: Int,
    val comment: String? = null
)

@Serializable
data class MyRatingResponse(
    val id: Long,
    val criteriaId: Long,
    val criteriaName: String,
    val score: String,
    val maxScore: Int,
    val comment: String? = null,
    val updatedAt: String
)

@Serializable
data class JudgeProgressResponse(
    val jamId: String,
    val jamName: String,
    val totalProjects: Int,
    val ratedProjects: Int,
    val missingProjects: List<MissingProjectInfo>
)

@Serializable
data class MissingProjectInfo(
    val projectId: String,
    val projectTitle: String,
    val teamName: String? = null,
    val missingCriteria: List<String>
)