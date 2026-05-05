package software.setixx.chimu.presentation.jam.details

import software.setixx.chimu.api.domain.UserRole
import software.setixx.chimu.domain.model.*

data class JamDetailsState(
    val jamDetails: GameJamDetails? = null,
    val registrations: List<Registration> = emptyList(),
    val judges: List<Judge> = emptyList(),
    val criteria: List<RatingCriteria> = emptyList(),
    val userTeams: List<Team> = emptyList(),
    
    val isLoading: Boolean = false,
    val isActionLoading: Boolean = false,
    val errorMessage: String? = null,
    
    val isDeleting: Boolean = false,
    val isDeleted: Boolean = false,
    
    val userRole: UserRole? = null,
    val userId: String? = null
) {
    val canEdit: Boolean = (userRole == UserRole.ADMIN) || (userRole == UserRole.ORGANIZER && jamDetails?.organizerId == userId)
    val isParticipant: Boolean = userRole == UserRole.PARTICIPANT
    
    fun isTeamRegistered(teamId: String): Boolean {
        return registrations.any { it.teamId == teamId && it.status != "WITHDRAWN" }
    }
}