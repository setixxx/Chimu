package software.setixx.chimu.presentation.jam.details.management

import software.setixx.chimu.domain.model.Judge
import software.setixx.chimu.domain.model.RatingCriteria
import software.setixx.chimu.domain.model.Registration

data class ManagementState(
    val criteria: List<RatingCriteria> = emptyList(),
    val judges: List<Judge> = emptyList(),
    val registrations: List<Registration> = emptyList(),
    val hasBanner: Boolean = false,
    val isPublished: Boolean = false,
    val isLoading: Boolean = false,
    val isActionLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)