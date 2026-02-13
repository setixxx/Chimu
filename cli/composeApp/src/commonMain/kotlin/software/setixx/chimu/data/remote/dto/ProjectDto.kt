package software.setixx.chimu.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProjectResponse(
    val id: String,
    val jamId: String,
    val jamName: String,
    val teamId: String? = null,
    val teamName: String? = null,
    val title: String,
    val description: String? = null,
    val gameUrl: String? = null,
    val repositoryUrl: String? = null,
    val status: String,
    val submittedAt: String? = null,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class ProjectDetailsResponse(
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
    val updatedAt: String,
    val canEdit: Boolean,
    val canSubmit: Boolean,
    val canDelete: Boolean
)

@Serializable
data class CreateProjectRequest(
    val title: String,
    val description: String? = null,
    val gameUrl: String? = null,
    val repositoryUrl: String? = null
)

@Serializable
data class UpdateProjectRequest(
    val title: String? = null,
    val description: String? = null,
    val gameUrl: String? = null,
    val repositoryUrl: String? = null
)