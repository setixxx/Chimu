package software.setixx.chimu.api.dto

import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CriteriaResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val maxScore: Int,
    val weight: String,
    val orderIndex: Int
)

data class CreateCriteriaRequest(
    @field:NotBlank(message = "Criteria name is required")
    @field:Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    val name: String,

    @field:Size(max = 500, message = "Description must not exceed 500 characters")
    val description: String? = null,

    @field:Min(value = 1, message = "Max score must be at least 1")
    @field:Max(value = 100, message = "Max score must not exceed 100")
    val maxScore: Int = 10,

    @field:DecimalMin(value = "0.1", message = "Weight must be at least 0.1")
    @field:DecimalMax(value = "10.0", message = "Weight must not exceed 10.0")
    val weight: Double = 1.0,

    @field:Min(value = 0, message = "Order index must be non-negative")
    val orderIndex: Int = 0
)

data class UpdateCriteriaRequest(
    @field:Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    val name: String? = null,

    @field:Size(max = 500, message = "Description must not exceed 500 characters")
    val description: String? = null,

    @field:Min(value = 1, message = "Max score must be at least 1")
    @field:Max(value = 100, message = "Max score must not exceed 100")
    val maxScore: Int? = null,

    @field:DecimalMin(value = "0.1", message = "Weight must be at least 0.1")
    @field:DecimalMax(value = "10.0", message = "Weight must not exceed 10.0")
    val weight: Double? = null,

    @field:Min(value = 0, message = "Order index must be non-negative")
    val orderIndex: Int? = null
)