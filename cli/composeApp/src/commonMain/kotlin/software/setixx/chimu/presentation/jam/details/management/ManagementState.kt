package software.setixx.chimu.presentation.jam.details.management

import software.setixx.chimu.domain.model.*

data class ManagementState(
    val jam: GameJamDetails? = null,
    val judges: List<Judge> = emptyList(),
    val registrations: List<Registration> = emptyList(),
    val criteria: List<RatingCriteria> = emptyList(),
    val jamProjects: List<Project> = emptyList(),
    val statistics: JamStatistics? = null,
    val leaderboard: Leaderboard? = null,
    val isLoading: Boolean = false,
    val isActionLoading: Boolean = false,
    val isPublished: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
) {
    val projectsByTeam: Map<String, List<Project>>
        get() = jamProjects.groupBy { it.teamId ?: "" }
}
