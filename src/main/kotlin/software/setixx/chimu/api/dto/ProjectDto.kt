package software.setixx.chimu.api.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import software.setixx.chimu.api.domain.ProjectStatus
import java.time.Instant

data class CreateProjectRequest(
    @field:NotBlank(message = "Project title is required")
    @field:Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    val title: String,

    @field:Size(max = 5000, message = "Description must not exceed 5000 characters")
    val description: String? = null,

    val gameUrl: String? = null,

    val repositoryUrl: String? = null
)

data class UpdateProjectRequest(
    @field:Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    val title: String? = null,

    @field:Size(max = 5000, message = "Description must not exceed 5000 characters")
    val description: String? = null,

    val gameUrl: String? = null,

    val repositoryUrl: String? = null
)

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
    val status: ProjectStatus,
    val submittedAt: String?,
    val createdAt: String,
    val updatedAt: String
)

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
    val status: ProjectStatus,
    val submittedAt: String?,
    val createdAt: String,
    val updatedAt: String,
    val canEdit: Boolean,
    val canSubmit: Boolean,
    val canDelete: Boolean
)