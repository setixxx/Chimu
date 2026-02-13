package software.setixx.chimu.api.dto

import jakarta.validation.constraints.*
import java.math.BigDecimal

data class RateProjectRequest(
    @field:NotNull(message = "Criteria ID is required")
    val criteriaId: Long,

    @field:NotNull(message = "Score is required")
    @field:DecimalMin(value = "0.0", message = "Score must be at least 0")
    val score: BigDecimal,

    @field:Size(max = 1000, message = "Comment must not exceed 1000 characters")
    val comment: String? = null
)

data class UpdateRatingRequest(
    @field:NotNull(message = "Score is required")
    @field:DecimalMin(value = "0.0", message = "Score must be at least 0")
    val score: BigDecimal,

    @field:Size(max = 1000, message = "Comment must not exceed 1000 characters")
    val comment: String? = null
)

data class RatingResponse(
    val id: Long,
    val projectId: String,
    val judgeId: String,
    val judgeNickname: String,
    val criteriaId: Long,
    val criteriaName: String,
    val score: String,
    val maxScore: Int,
    val comment: String?,
    val createdAt: String,
    val updatedAt: String
)

data class MyRatingResponse(
    val id: Long,
    val criteriaId: Long,
    val criteriaName: String,
    val score: String,
    val maxScore: Int,
    val comment: String?,
    val updatedAt: String
)

data class ProjectRatingSummaryResponse(
    val projectId: String,
    val criteriaRatings: List<CriteriaRatingSummary>
)

data class CriteriaRatingSummary(
    val criteriaId: Long,
    val criteriaName: String,
    val maxScore: Int,
    val weight: String,
    val averageScore: String,
    val judgeRatings: List<JudgeRating>
)

data class JudgeRating(
    val judgeNickname: String,
    val score: String,
    val comment: String?
)

data class JudgeProgressResponse(
    val jamId: String,
    val jamName: String,
    val totalProjects: Int,
    val ratedProjects: Int,
    val missingProjects: List<MissingProjectInfo>
)

data class MissingProjectInfo(
    val projectId: String,
    val projectTitle: String,
    val teamName: String?,
    val missingCriteria: List<String>
)