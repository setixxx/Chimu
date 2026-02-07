package software.setixx.chimu.presentation.jam.details

import software.setixx.chimu.domain.model.GameJamDetails
import software.setixx.chimu.domain.model.Judge
import software.setixx.chimu.domain.model.Registration
import software.setixx.chimu.domain.model.Team

data class JamDetailsState(
    val jamDetails: GameJamDetails? = null,
    val registrations: List<Registration> = emptyList(),
    val judges: List<Judge> = emptyList(),
    val userTeams: List<Team> = emptyList(),
    
    val isLoading: Boolean = false,
    val isActionLoading: Boolean = false,
    val errorMessage: String? = null,
    
    val isDeleting: Boolean = false,
    val isDeleted: Boolean = false,
    
    val userRole: String? = null,
    val userId: String? = null
) {
    val canEdit: Boolean = (userRole == "ADMIN") || (userRole == "ORGANIZER" && jamDetails?.organizerId == userId)
    val isParticipant: Boolean = userRole == "PARTICIPANT"
    
    fun isTeamRegistered(teamId: String): Boolean {
        return registrations.any { it.teamId == teamId && it.status != "WITHDRAWN" }
    }
}