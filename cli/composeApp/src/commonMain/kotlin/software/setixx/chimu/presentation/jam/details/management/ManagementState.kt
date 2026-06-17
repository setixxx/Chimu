package software.setixx.chimu.presentation.jam.details.management

import software.setixx.chimu.domain.model.*

/**
 * Состояние экрана управления джемом.
 * Хранит полные данные о регистрации команд, списки судей и критерии.
 */
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
    val successMessage: String? = null,
    val isBannerUploading: Boolean = false,
    val isBannerDeleting: Boolean = false,


    val judgeSearchQuery: String = "",
    val foundJudge: PublicUserProfile? = null,
    val isSearchingJudge: Boolean = false,
    val judgeSearchError: String? = null
) {
    val projectsByTeam: Map<String, List<Project>>
        get() = jamProjects.groupBy { it.teamId ?: "" }
}
