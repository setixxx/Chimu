package software.setixx.chimu.api.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import software.setixx.chimu.api.domain.*
import software.setixx.chimu.api.dto.*
import software.setixx.chimu.api.repository.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID

@Service
class LeaderboardService(
    private val gameJamRepository: GameJamRepository,
    private val projectRepository: ProjectRepository,
    private val ratingRepository: RatingRepository,
    private val ratingCriteriaRepository: RatingCriteriaRepository,
    private val jamJudgeRepository: JamJudgeRepository,
    private val teamRepository: TeamRepository,
    private val userRepository: UserRepository
) {

    @Transactional(readOnly = true)
    fun getLeaderboard(jamId: String, userId: Long?): LeaderboardResponse {
        val jam = gameJamRepository.findByPublicId(UUID.fromString(jamId))
            ?: throw IllegalArgumentException("Game jam not found")

        val canView = when (jam.status) {
            GameJamStatus.COMPLETED -> true
            GameJamStatus.JUDGING -> {
                if (userId == null) false
                else {
                    val user = userRepository.findById(userId).orElse(null)
                    jam.organizerId == userId || user?.role == UserRole.ADMIN
                }
            }
            else -> false
        }

        if (!canView) {
            throw IllegalArgumentException("Leaderboard is only visible during judging (for organizers) or after completion")
        }

        val projects = projectRepository.findPublishedProjectsByJamId(jam.id!!)
        val criteria = ratingCriteriaRepository.findAllByJamIdOrderByOrderIndex(jam.id!!)
        val allRatings = ratingRepository.findAllByProjectIdIn(projects.map { it.id!! })
        val totalJudges = jamJudgeRepository.countByJamId(jam.id!!).toInt()

        val projectScores = projects.mapNotNull { project ->
            calculateProjectScore(project, criteria, allRatings, totalJudges)
        }.filter { it.score.allCriteriaRated }

        val sortedProjects = projectScores.sortedWith(
            compareByDescending<ProjectRanking> { it.score.total.toBigDecimal() }
                .thenByDescending { it.score.judgesRated }
                .thenBy { it.project.submittedAt }
        ).mapIndexed { index, projectRanking ->
            projectRanking.copy(rank = index + 1)
        }

        return LeaderboardResponse(
            jamId = jam.publicId.toString(),
            jamName = jam.name,
            jamStatus = jam.status.toString(),
            totalProjects = projects.size,
            qualifiedProjects = sortedProjects.size,
            totalJudges = totalJudges,
            rankings = sortedProjects
        )
    }

    @Transactional(readOnly = true)
    fun getJamStatistics(jamId: String, userId: Long): JamStatisticsResponse {
        val jam = gameJamRepository.findByPublicId(UUID.fromString(jamId))
            ?: throw IllegalArgumentException("Game jam not found")

        val user = userRepository.findById(userId).orElseThrow()

        if (jam.organizerId != userId && user.role != UserRole.ADMIN) {
            throw IllegalArgumentException("Only organizer or admin can view statistics")
        }

        val allProjects = projectRepository.findAllByJamId(jam.id!!)
        val publishedProjects = allProjects.filter { it.status == ProjectStatus.PUBLISHED }
        val disqualifiedProjects = allProjects.filter { it.status == ProjectStatus.DISQUALIFIED }

        val criteria = ratingCriteriaRepository.findAllByJamIdOrderByOrderIndex(jam.id!!)
        val allRatings = ratingRepository.findAllByProjectIdIn(publishedProjects.map { it.id!! })

        val averageScoresPerCriteria = criteria.map { criterion ->
            val criterionRatings = allRatings.filter { it.criteriaId == criterion.id }
            val avgScore = if (criterionRatings.isNotEmpty()) {
                criterionRatings.map { it.score }
                    .reduce { acc, score -> acc.add(score) }
                    .divide(BigDecimal(criterionRatings.size), 2, RoundingMode.HALF_UP)
            } else {
                BigDecimal.ZERO
            }

            CriteriaAverageScore(
                criteriaName = criterion.name,
                averageScore = avgScore.toString(),
                maxScore = criterion.maxScore
            )
        }

        val judges = jamJudgeRepository.findAllByJamId(jam.id!!)
        val judgeUsers = userRepository.findAllById(judges.map { it.judgeId }).associateBy { it.id }

        val judgeCompletion = judges.map { jamJudge ->
            val judgeId = jamJudge.judgeId
            val ratedProjects = ratingRepository.countRatedProjectsByJudgeAndJam(judgeId, jam.id!!).toInt()
            val totalProjects = publishedProjects.size
            val percentage = if (totalProjects > 0) {
                (ratedProjects * 100) / totalProjects
            } else {
                0
            }

            val judge = judgeUsers[judgeId]!!
            JudgeCompletion(
                judgeNickname = judge.nickname,
                ratedProjects = ratedProjects,
                totalProjects = totalProjects,
                completionPercentage = percentage
            )
        }

        return JamStatisticsResponse(
            jamId = jam.publicId.toString(),
            jamName = jam.name,
            totalProjects = allProjects.size,
            publishedProjects = publishedProjects.size,
            disqualifiedProjects = disqualifiedProjects.size,
            totalJudges = judges.size,
            averageScoresPerCriteria = averageScoresPerCriteria,
            judgeCompletionRate = judgeCompletion
        )
    }

    private fun calculateProjectScore(
        project: Project,
        criteria: List<RatingCriteria>,
        allRatings: List<Rating>,
        totalJudges: Int
    ): ProjectRanking? {
        val projectRatings = allRatings.filter { it.projectId == project.id }

        val criteriaScores = criteria.map { criterion ->
            val criterionRatings = projectRatings.filter { it.criteriaId == criterion.id }

            if (criterionRatings.isEmpty()) {
                return null
            }

            val scores = criterionRatings.map { it.score }
            val avgScore = scores.reduce { acc, score -> acc.add(score) }
                .divide(BigDecimal(scores.size), 2, RoundingMode.HALF_UP)
            val weightedScore = avgScore.multiply(criterion.weight)
                .setScale(2, RoundingMode.HALF_UP)

            CriteriaScoreDetail(
                criteriaId = criterion.id!!,
                criteriaName = criterion.name,
                weight = criterion.weight.toString(),
                maxScore = criterion.maxScore,
                averageScore = avgScore.toString(),
                weightedScore = weightedScore.toString(),
                judgeCount = scores.size,
                scores = scores.map { it.setScale(2, RoundingMode.HALF_UP).toString() }
            )
        }

        val allCriteriaRated = criteriaScores.all { it.judgeCount > 0 }
        if (!allCriteriaRated) {
            return null
        }

        val totalScore = criteriaScores
            .map { it.weightedScore.toBigDecimal() }
            .reduce { acc, score -> acc.add(score) }
            .setScale(2, RoundingMode.HALF_UP)

        val judgesRated = projectRatings.map { it.judgeId }.distinct().size

        val team = project.teamId?.let { teamRepository.findById(it).orElse(null) }

        return ProjectRanking(
            rank = 0,
            project = ProjectInfo(
                id = project.publicId.toString(),
                title = project.title,
                description = project.description,
                teamId = team?.publicId?.toString(),
                teamName = team?.name,
                gameUrl = project.gameUrl,
                repositoryUrl = project.repositoryUrl,
                submittedAt = project.submittedAt?.toString()
            ),
            score = ScoreBreakdown(
                total = totalScore.toString(),
                breakdown = criteriaScores,
                allCriteriaRated = allCriteriaRated,
                judgesRated = judgesRated,
                totalJudges = totalJudges
            )
        )
    }
}