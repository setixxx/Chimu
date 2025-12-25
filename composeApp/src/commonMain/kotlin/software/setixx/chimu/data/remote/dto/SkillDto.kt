package software.setixx.chimu.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SkillResponse(
    val id: Long,
    val name: String
)