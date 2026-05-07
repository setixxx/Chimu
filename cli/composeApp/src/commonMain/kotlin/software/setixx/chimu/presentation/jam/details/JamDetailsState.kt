package software.setixx.chimu.presentation.jam.details

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
    val canEdit: Boolean
        get() = userRole == UserRole.ADMIN ||
                (userRole == UserRole.ORGANIZER && jamDetails?.organizerId == userId)

    val isAdminOrOrganizer: Boolean
        get() = userRole == UserRole.ADMIN ||
                (userRole == UserRole.ORGANIZER && jamDetails?.organizerId == userId)

    val isParticipant: Boolean
        get() = userRole == UserRole.PARTICIPANT

    val isJudge: Boolean
        get() = userRole == UserRole.JUDGE
}