package software.setixx.chimu.presentation.jam.details.judging

import software.setixx.chimu.domain.model.JudgeProgress
import software.setixx.chimu.domain.model.Project

data class JudgingState(
    val projects: List<Project> = emptyList(),
    val judgeProgress: JudgeProgress? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)