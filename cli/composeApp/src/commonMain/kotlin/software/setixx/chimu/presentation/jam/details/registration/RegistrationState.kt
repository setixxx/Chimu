package software.setixx.chimu.presentation.jam.details.registration

import software.setixx.chimu.api.domain.GameJamStatus
import software.setixx.chimu.domain.model.GameJamDetails
import software.setixx.chimu.domain.model.Registration
import software.setixx.chimu.domain.model.Team

data class RegistrationState(
    val registrations: List<Registration> = emptyList(),
    val userTeams: List<Team> = emptyList(),
    val isLoading: Boolean = false,
    val isActionLoading: Boolean = false,
    val errorMessage: String? = null
) {
    fun isTeamRegistered(teamId: String): Boolean =
        registrations.any { it.teamId == teamId && it.status != "WITHDRAWN" }

    fun canTeamRegister(jam: GameJamDetails): Boolean = jam.status == GameJamStatus.REGISTRATION_OPEN
}
