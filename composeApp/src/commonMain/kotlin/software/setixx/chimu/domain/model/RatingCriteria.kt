package software.setixx.chimu.domain.model

data class RatingCriteria(
    val id: Long,
    val name: String,
    val description: String?,
    val maxScore: Int,
    val weight: String,
    val orderIndex: Int
)

data class CreateRatingCriteria(
    val name: String,
    val description: String?,
    val maxScore: Int,
    val weight: Double,
    val orderIndex: Int
)

data class UpdateRatingCriteria(
    val name: String?,
    val description: String?,
    val maxScore: Int?,
    val weight: Double?,
    val orderIndex: Int?
)