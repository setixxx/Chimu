package software.setixx.chimu.presentation.jam.details.overview

import software.setixx.chimu.api.domain.GameJamStatus
import software.setixx.chimu.api.domain.RegistrationStatus
import software.setixx.chimu.domain.model.GameJamDetails
import software.setixx.chimu.domain.model.Registration
import software.setixx.chimu.domain.model.Team

/**
 * Состояние вкладки обзора Game Jam.
 * Отслеживает регистрацию пользовательских команд и возможность участия в мероприятии.
 */
data class OverviewState(
    val registrations: List<Registration> = emptyList(),
    val userTeams: List<Team> = emptyList(),
    val isLoading: Boolean = false,
    val isActionLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val registeredTeam: Team?
        get() = userTeams.find { isTeamRegistered(it.id) }

    val registeredTeamId: String?
        get() = registeredTeam?.id

    val hasRegisteredTeam: Boolean
        get() = registeredTeamId != null

    val isLeaderOfRegisteredTeam: Boolean
        get() = registeredTeam?.isLeader == true

    fun isTeamRegistered(teamId: String): Boolean =
        registrations.any { it.teamId == teamId && it.status !in listOf(RegistrationStatus.WITHDRAWN, RegistrationStatus.CANCELLED) }

    fun canTeamRegister(jam: GameJamDetails): Boolean = jam.status == GameJamStatus.REGISTRATION_OPEN
}
