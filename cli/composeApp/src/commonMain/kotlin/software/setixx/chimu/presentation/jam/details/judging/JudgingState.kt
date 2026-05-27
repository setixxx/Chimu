package software.setixx.chimu.presentation.jam.details.judging

import software.setixx.chimu.domain.model.JudgeProgress
import software.setixx.chimu.domain.model.MyRating
import software.setixx.chimu.domain.model.Project
import software.setixx.chimu.data.remote.dto.RatingCriteriaResponse

data class JudgingState(
    val projects: List<Project> = emptyList(),
    val judgeProgress: JudgeProgress? = null,
    val selectedProject: Project? = null,
    val jamCriteria: List<RatingCriteriaResponse> = emptyList(),
    val myRatings: List<MyRating> = emptyList(),
    val userProject: Project? = null,
    val isLoading: Boolean = false,
    val isRatingLoading: Boolean = false,
    val isActionLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
