package software.setixx.chimu.domain.model

data class Leaderboard(
    val jamId: String,
    val jamName: String,
    val jamStatus: String,
    val totalProjects: Int,
    val qualifiedProjects: Int,
    val totalJudges: Int,
    val rankings: List<ProjectRanking>
)

data class ProjectRanking(
    val rank: Int,
    val project: ProjectInfo,
    val score: ScoreBreakdown
)

data class ProjectInfo(
    val id: String,
    val title: String,
    val description: String?,
    val teamId: String?,
    val teamName: String?,
    val gameUrl: String?,
    val repositoryUrl: String?,
    val submittedAt: String?
)

data class ScoreBreakdown(
    val total: String,
    val breakdown: List<CriteriaScoreDetail>,
    val allCriteriaRated: Boolean,
    val judgesRated: Int,
    val totalJudges: Int
)

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

data class JamStatistics(
    val jamId: String,
    val jamName: String,
    val totalProjects: Int,
    val publishedProjects: Int,
    val disqualifiedProjects: Int,
    val totalJudges: Int,
    val averageScoresPerCriteria: List<CriteriaAverageScore>,
    val judgeCompletionRate: List<JudgeCompletion>
)

data class CriteriaAverageScore(
    val criteriaName: String,
    val averageScore: String,
    val maxScore: Int
)

data class JudgeCompletion(
    val judgeNickname: String,
    val ratedProjects: Int,
    val totalProjects: Int,
    val completionPercentage: Int
)