package software.setixx.chimu.presentation.jam.details.progress

import software.setixx.chimu.api.domain.UserRole
import software.setixx.chimu.domain.model.Project
import software.setixx.chimu.domain.model.ProjectDetails
import software.setixx.chimu.domain.model.ProjectFile
import software.setixx.chimu.domain.model.Registration
import software.setixx.chimu.domain.model.JamStatistics
import software.setixx.chimu.domain.model.Team

data class ProgressState(
    val userProject: ProjectDetails? = null,
    val projectFiles: List<ProjectFile> = emptyList(),
    val allProjects: List<Project> = emptyList(),
    val statistics: JamStatistics? = null,
    val userTeams: List<Team> = emptyList(),
    val registrations: List<Registration> = emptyList(),
    val isLoading: Boolean = false,
    val isActionLoading: Boolean = false,
    val errorMessage: String? = null
) {
    fun getUserRegistration(): Registration? =
        registrations.find { reg ->
            userTeams.any { it.id == reg.teamId } && reg.status == "APPROVED"
        }

    fun isUserLeaderOfRegisteredTeam(): Boolean {
        val reg = getUserRegistration() ?: return false
        return userTeams.find { it.id == reg.teamId }?.isLeader == true
    }
}
