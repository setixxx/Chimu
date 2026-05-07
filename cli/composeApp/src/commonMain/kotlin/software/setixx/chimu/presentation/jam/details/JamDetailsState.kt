package software.setixx.chimu.presentation.jam.details

import software.setixx.chimu.api.domain.GameJamStatus
import software.setixx.chimu.api.domain.UserRole
import software.setixx.chimu.domain.model.GameJamDetails

data class JamDetailsState(
    val jamDetails: GameJamDetails? = null,
    val userRole: UserRole? = null,
    val userId: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isDeleting: Boolean = false,
    val isDeleted: Boolean = false
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
        )

    val isAdminOrOrganizer: Boolean
        get() = userRole == UserRole.ADMIN ||
                (userRole == UserRole.ORGANIZER && userId == jamDetails?.organizerId)

    val isParticipant: Boolean
        get() = userRole == UserRole.PARTICIPANT

    val isJudge: Boolean
        get() = userRole == UserRole.JUDGE
}
