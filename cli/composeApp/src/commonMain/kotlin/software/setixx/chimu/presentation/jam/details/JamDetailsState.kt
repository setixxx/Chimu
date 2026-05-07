package software.setixx.chimu.presentation.jam.details

import software.setixx.chimu.api.domain.UserRole
import software.setixx.chimu.domain.model.*

data class JamDetailsState(
    val jamDetails: GameJamDetails? = null,
    val registrations: List<Registration> = emptyList(),
    val judges: List<Judge> = emptyList(),
    val criteria: List<RatingCriteria> = emptyList(),
    val userTeams: List<Team> = emptyList(),
    val userProject: ProjectDetails? = null,
    val projectFiles: List<ProjectFile> = emptyList(),
    val allProjects: List<Project> = emptyList(),
    
    val isLoading: Boolean = false,
    val isActionLoading: Boolean = false,
    val errorMessage: String? = null,
    
    val isDeleting: Boolean = false,
    val isDeleted: Boolean = false,
    
    val userRole: UserRole? = null,
    val userId: String? = null
) {
    val canEdit: Boolean = (userRole == UserRole.ADMIN) || (userRole == UserRole.ORGANIZER && jamDetails?.organizerId == userId)
    val isAdminOrOrganizer: Boolean = userRole == UserRole.ADMIN || userRole == UserRole.ORGANIZER
    val isParticipant: Boolean = userRole == UserRole.PARTICIPANT
    val isJudge: Boolean = userRole == UserRole.JUDGE
    
    fun isTeamRegistered(teamId: String): Boolean {
        return registrations.any { it.teamId == teamId && it.status != "WITHDRAWN" }
    }

    fun getUserRegistration(): Registration? {
        return registrations.find { reg -> 
            userTeams.any { it.id == reg.teamId } && reg.status == "APPROVED"
        }
    }

    fun isUserLeaderOfRegisteredTeam(): Boolean {
        val reg = getUserRegistration() ?: return false
        return userTeams.find { it.id == reg.teamId }?.isLeader == true
    }
}