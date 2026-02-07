package software.setixx.chimu.presentation.jam.details

import software.setixx.chimu.domain.model.GameJamDetails

data class JamDetailsState(
    val jamDetails: GameJamDetails? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isDeleting: Boolean = false,
    val isDeleted: Boolean = false,
    val userRole: String? = null,
    val userId: String? = null
) {
    val canEdit: Boolean = (userRole == "ADMIN") || (userRole == "ORGANIZER" && jamDetails?.organizerId == userId)
}