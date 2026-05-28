package software.setixx.chimu.presentation.team.details

import software.setixx.chimu.domain.model.Specialization
import software.setixx.chimu.domain.model.TeamDetails
import software.setixx.chimu.domain.model.TeamMember
import software.setixx.chimu.domain.model.UserProfile

/**
 * Состояние экрана деталей команды.
 * Хранит информацию о составе, токенах приглашения и управляет видимостью диалоговых окон.
 */
data class TeamDetailsState(
    val user: UserProfile? = null,
    val team: TeamDetails? = null,
    val isLoading: Boolean = false,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,

    val editName: String = "",
    val editDescription: String = "",
    val nameError: String? = null,

    val showLeaveDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val showKickDialog: Boolean = false,
    val memberToKick: TeamMember? = null,

    val showInviteDialog: Boolean = false,
    val showJoinDialog: Boolean = false,
    val inviteToken: String = "",

    val showSpecializationDialog: Boolean = false,
    val availableSpecializations: List<Specialization> = emptyList(),
    val selectedSpecialization: Specialization? = null
){
    fun isCurrentUserLeader(): Boolean {
        return team?.leaderId == user?.id
    }

    fun getCurrentUserMember(): TeamMember? {
        return team?.members?.find { it.userId == user?.id }
    }
}