package software.setixx.chimu.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SpecializationResponse(
    val id: String,
    val name: String,
    val description: String? = null
)