package software.setixx.chimu.api.dto

/**
 * Ответ с информацией о специализации.
 */
data class SpecializationResponse(
    val id: String,
    val name: String,
    val description: String?
)