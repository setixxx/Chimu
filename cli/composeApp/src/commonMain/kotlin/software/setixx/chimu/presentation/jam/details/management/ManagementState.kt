package software.setixx.chimu.presentation.jam.details.management

import software.setixx.chimu.domain.model.JamStatistics
import software.setixx.chimu.domain.model.Judge
import software.setixx.chimu.domain.model.Leaderboard
import software.setixx.chimu.domain.model.Project
import software.setixx.chimu.domain.model.RatingCriteria
import software.setixx.chimu.domain.model.Registration

data class ManagementState(
    val judges: List<Judge> = emptyList(),
    val registrations: List<Registration> = emptyList(),
    val criteria: List<RatingCriteria> = emptyList(),
    val jamProjects: List<Project> = emptyList(),
    val statistics: JamStatistics? = null,
    val leaderboard: Leaderboard? = null,
    val hasBanner: Boolean = false,
    val isLoading: Boolean = false,
    val isActionLoading: Boolean = false,
    val isPublished: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
) {
    /** Projects grouped by teamId for use in the expandable teams list. */
    val projectsByTeam: Map<String, List<Project>>
        get() = jamProjects.groupBy { it.teamId ?: "" }
}