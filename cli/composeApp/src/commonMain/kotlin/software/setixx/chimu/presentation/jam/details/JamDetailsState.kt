package software.setixx.chimu.presentation.jam.details

import software.setixx.chimu.api.domain.GameJamStatus
import software.setixx.chimu.api.domain.RegistrationStatus
import software.setixx.chimu.api.domain.TransferStatus
import software.setixx.chimu.api.domain.UserRole
import software.setixx.chimu.domain.model.GameJamDetails
import software.setixx.chimu.domain.model.JamTransfer
import software.setixx.chimu.domain.model.PublicUserProfile
import software.setixx.chimu.domain.model.Registration
import software.setixx.chimu.domain.model.Team

/**
 * Состояние экрана деталей Game Jam.
 * Определяет доступные вкладки и действия в зависимости от роли пользователя.
 */
data class JamDetailsState(
    val jamDetails: GameJamDetails? = null,
    val userRole: UserRole? = null,
    val userId: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isDeleting: Boolean = false,
    val isDeleted: Boolean = false,
    val isCancelled: Boolean = false,
    val isCancelling: Boolean = false,

    val showTransferDialog: Boolean = false,
    val currentTransfer: JamTransfer? = null,
    val isTransferActionLoading: Boolean = false,
    val transferError: String? = null,
    val transferRecipientQuery: String = "",
    val transferRecipientFound: PublicUserProfile? = null,
    val isSearchingRecipient: Boolean = false,
    val userTeams: List<Team> = emptyList(),
    val registrations: List<Registration> = emptyList(),

    val showForceStatusDialog: Boolean = false,
    val selectedForceStatus: GameJamStatus? = null,
    val isForceStatusActionIsLoading: Boolean = false,
    val forceStatusError: String? = null,


    ) {
    val canCancel: Boolean
        get() = jamDetails?.status in listOf(
            GameJamStatus.ANNOUNCED,
            GameJamStatus.REGISTRATION_OPEN,
            GameJamStatus.REGISTRATION_CLOSED
        ) && isAdminOrOrganizer

    val canDelete: Boolean
        get() = jamDetails?.status == GameJamStatus.DRAFT && isAdminOrOrganizer

    val canEdit: Boolean
        get() = jamDetails?.status in listOf(
            GameJamStatus.DRAFT,
            GameJamStatus.ANNOUNCED,
            GameJamStatus.REGISTRATION_OPEN,
            GameJamStatus.REGISTRATION_CLOSED
        ) && isAdminOrOrganizer

    val isAdminOrOrganizer: Boolean
        get() = userRole == UserRole.ADMIN ||
                (userRole == UserRole.ORGANIZER && userId == jamDetails?.organizerId)

    val isPreviousOrganizer: Boolean
        get() = currentTransfer?.senderId == userId

    val canTransferEnabled: Boolean
        get() = (isAdminOrOrganizer || isPreviousOrganizer) && jamDetails?.status !in listOf(
            GameJamStatus.DRAFT,
            GameJamStatus.COMPLETED
        )

    val hasPendingTransfer: Boolean
        get() = currentTransfer?.status == TransferStatus.PENDING

    val isParticipant: Boolean
        get() = userRole == UserRole.PARTICIPANT

    val isJudge: Boolean
        get() = userRole == UserRole.JUDGE

    val isAdmin: Boolean
        get() = userRole == UserRole.ADMIN

    val canTransferJam: Boolean
        get() = (isAdminOrOrganizer || isPreviousOrganizer) &&
                jamDetails?.status !in setOf(
                    GameJamStatus.DRAFT,
                    GameJamStatus.CANCELLED,
                    GameJamStatus.COMPLETED
                )

    val availableForceStatuses: List<GameJamStatus>
        get() = listOf(
            GameJamStatus.DRAFT,
            GameJamStatus.ANNOUNCED,
            GameJamStatus.REGISTRATION_OPEN,
            GameJamStatus.REGISTRATION_CLOSED,
            GameJamStatus.IN_PROGRESS,
            GameJamStatus.JUDGING,
            GameJamStatus.COMPLETED
        )

    val hasApprovedRegistration: Boolean
        get() = registrations.any { reg ->
            reg.status == RegistrationStatus.APPROVED && userTeams.any { it.id == reg.teamId }
        }

    val availableTabs: List<JamDetailsTab>
        get() {
            val tabs = mutableListOf(JamDetailsTab.Overview)

            if ((isParticipant && hasApprovedRegistration) || isAdminOrOrganizer) {
                tabs.add(JamDetailsTab.Project)
            }

            if (isJudge) {
                tabs.add(JamDetailsTab.Judging)
            }

            tabs.add(JamDetailsTab.Leaderboard)

            if (isAdminOrOrganizer) {
                tabs.add(JamDetailsTab.Management)
            }

            return tabs
        }
}
