package software.setixx.chimu.domain.model

data class Project(
    val id: String,
    val jamId: String,
    val jamName: String,
    val teamId: String? = null,
    val teamName: String? = null,
    val title: String,
    val description: String? = null,
    val gameUrl: String? = null,
    val repositoryUrl: String? = null,
    val status: ProjectStatus,
    val submittedAt: String? = null,
    val createdAt: String,
    val updatedAt: String
)

data class ProjectDetails(
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

data class CreateProject(
    val title: String,
    val description: String? = null,
    val gameUrl: String? = null,
    val repositoryUrl: String? = null
)

data class UpdateProject(
    val title: String? = null,
    val description: String? = null,
    val gameUrl: String? = null,
    val repositoryUrl: String? = null
)

enum class ProjectStatus {
    DRAFT,
    SUBMITTED,
    UNDER_REVIEW,
    PUBLISHED,
    DISQUALIFIED
}