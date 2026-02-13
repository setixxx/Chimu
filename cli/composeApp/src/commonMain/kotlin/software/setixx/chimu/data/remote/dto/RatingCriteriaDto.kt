package software.setixx.chimu.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RatingCriteriaResponse(
    val id: Long,
    val name: String,
    val description: String? = null,
    val maxScore: Int,
    val weight: String,
    val orderIndex: Int
)

@Serializable
data class CreateRatingCriteriaRequest(
    val name: String,
    val description: String? = null,
    val maxScore: Int,
    val weight: Double,
    val orderIndex: Int
)

@Serializable
data class UpdateRatingCriteriaRequest(
    val name: String? = null,
    val description: String? = null,
    val maxScore: Int? = null,
    val weight: Double? = null,
    val orderIndex: Int? = null
)