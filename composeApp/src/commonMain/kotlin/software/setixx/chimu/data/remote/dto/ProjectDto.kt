package software.setixx.chimu.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProjectResponse(
    val id: String,
    val jamId: String,
    val jamName: String,
    val teamId: String?,
    val teamName: String?,
    val title: String,
    val description: String?,
    val gameUrl: String?,
    val repositoryUrl: String?,
    val status: String,
    val submittedAt: String?,
    val createdAt: String,
    val updatedAt: String
)