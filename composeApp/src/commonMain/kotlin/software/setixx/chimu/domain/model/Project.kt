package software.setixx.chimu.domain.model

data class Project(
    val id: String,
    val jamName: String,
    val teamName: String?,
    val title: String,
    val status: ProjectStatus,
    val submittedAt: String?
)

enum class ProjectStatus {
    DRAFT,
    SUBMITTED,
    UNDER_REVIEW,
    PUBLISHED,
    DISQUALIFIED
}