package software.setixx.chimu.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LeaderboardResponse(
    val jamId: String,
    val jamName: String,
    val jamStatus: String,
    val totalProjects: Int,
    val qualifiedProjects: Int,
    val totalJudges: Int,
    val rankings: List<ProjectRankingResponse>
)

@Serializable
data class ProjectRankingResponse(
    val rank: Int,
    val project: ProjectInfo,
    val score: ScoreBreakdown
)

@Serializable
data class ProjectInfo(
    val id: String,
    val title: String,
    val description: String? = null,
    val teamId: String? = null,
    val teamName: String? = null,
    val gameUrl: String? = null,
    val repositoryUrl: String? = null,
    val submittedAt: String?
)

@Serializable
data class ScoreBreakdown(
    val total: String,
    val breakdown: List<CriteriaScoreDetail>,
    val allCriteriaRated: Boolean,
    val judgesRated: Int,
    val totalJudges: Int
)

@Serializable
data class CriteriaScoreDetail(
    val criteriaId: Long,
    val criteriaName: String,
    val weight: String,
    val maxScore: Int,
    val averageScore: String,
    val weightedScore: String,
    val judgeCount: Int,
    val scores: List<String>
)

@Serializable
data class JamStatisticsResponse(
    val jamId: String,
    val jamName: String,
    val totalProjects: Int,
    val publishedProjects: Int,
    val disqualifiedProjects: Int,
    val totalJudges: Int,
    val averageScoresPerCriteria: List<CriteriaAverageScore>,
    val judgeCompletionRate: List<JudgeCompletion>
)

@Serializable
data class CriteriaAverageScore(
    val criteriaName: String,
    val averageScore: String,
    val maxScore: Int
)

@Serializable
data class JudgeCompletion(
    val judgeNickname: String,
    val ratedProjects: Int,
    val totalProjects: Int,
    val completionPercentage: Int
)