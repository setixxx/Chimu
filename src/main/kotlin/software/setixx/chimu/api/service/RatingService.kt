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
class RatingService(
    private val ratingRepository: RatingRepository,
    private val projectRepository: ProjectRepository,
    private val gameJamRepository: GameJamRepository,
    private val ratingCriteriaRepository: RatingCriteriaRepository,
    private val jamJudgeRepository: JamJudgeRepository,
    private val teamRepository: TeamRepository,
    private val userRepository: UserRepository
) {

    @Transactional
    fun rateProject(projectId: String, userId: Long, request: RateProjectRequest): RatingResponse {
        val project = projectRepository.findByPublicId(UUID.fromString(projectId))
            ?: throw IllegalArgumentException("Project not found")

        val jam = gameJamRepository.findById(project.jamId).orElseThrow()
        val user = userRepository.findById(userId).orElseThrow()

        if (jam.status != GameJamStatus.JUDGING) {
            throw IllegalArgumentException("Ratings can only be submitted during judging phase")
        }

        if (project.status != ProjectStatus.PUBLISHED) {
            throw IllegalArgumentException("Only published projects can be rated")
        }

        val isJudge = jamJudgeRepository.existsByJamIdAndJudgeId(jam.id!!, userId)
        if (!isJudge) {
            throw IllegalArgumentException("You are not assigned as a judge for this jam")
        }

        val criteria = ratingCriteriaRepository.findById(request.criteriaId)
            .orElseThrow { IllegalArgumentException("Criteria not found") }

        if (criteria.jamId != jam.id) {
            throw IllegalArgumentException("Criteria does not belong to this jam")
        }

        validateScore(request.score, criteria.maxScore)

        val existingRating = ratingRepository.findByProjectIdAndJudgeIdAndCriteriaId(
            project.id!!, userId, criteria.id!!
        )

        val rating = existingRating?.apply {
            score = request.score
            comment = request.comment
        } ?: Rating(
            projectId = project.id!!,
            judgeId = userId,
            criteriaId = criteria.id!!,
            score = request.score,
            comment = request.comment
        )

        ratingRepository.save(rating)

        return toRatingResponse(rating, project, user, criteria)
    }

    @Transactional
    fun updateRating(ratingId: Long, userId: Long, request: UpdateRatingRequest): RatingResponse {
        val rating = ratingRepository.findById(ratingId)
            .orElseThrow { IllegalArgumentException("Rating not found") }

        if (rating.judgeId != userId) {
            throw IllegalArgumentException("You can only update your own ratings")
        }

        val project = projectRepository.findById(rating.projectId).orElseThrow()
        val jam = gameJamRepository.findById(project.jamId).orElseThrow()

        if (jam.status != GameJamStatus.JUDGING) {
            throw IllegalArgumentException("Ratings can only be updated during judging phase")
        }

        val criteria = ratingCriteriaRepository.findById(rating.criteriaId).orElseThrow()
        validateScore(request.score, criteria.maxScore)

        rating.score = request.score
        rating.comment = request.comment
        ratingRepository.save(rating)

        val user = userRepository.findById(userId).orElseThrow()
        return toRatingResponse(rating, project, user, criteria)
    }

    @Transactional
    fun deleteRating(ratingId: Long, userId: Long) {
        val rating = ratingRepository.findById(ratingId)
            .orElseThrow { IllegalArgumentException("Rating not found") }

        if (rating.judgeId != userId) {
            throw IllegalArgumentException("You can only delete your own ratings")
        }

        val project = projectRepository.findById(rating.projectId).orElseThrow()
        val jam = gameJamRepository.findById(project.jamId).orElseThrow()

        if (jam.status != GameJamStatus.JUDGING) {
            throw IllegalArgumentException("Ratings can only be deleted during judging phase")
        }

        ratingRepository.delete(rating)
    }

    @Transactional(readOnly = true)
    fun getProjectRatings(projectId: String, userId: Long): ProjectRatingSummaryResponse {
        val project = projectRepository.findByPublicId(UUID.fromString(projectId))
            ?: throw IllegalArgumentException("Project not found")

        val jam = gameJamRepository.findById(project.jamId).orElseThrow()
        val user = userRepository.findById(userId).orElseThrow()

        val canViewAllRatings = jam.status == GameJamStatus.COMPLETED ||
                jam.organizerId == userId ||
                user.role == UserRole.ADMIN

        if (!canViewAllRatings) {
            throw IllegalArgumentException("Full ratings are only visible after jam completion")
        }

        val ratings = ratingRepository.findAllByProjectId(project.id!!)
        val criteria = ratingCriteriaRepository.findAllByJamIdOrderByOrderIndex(jam.id!!)
        val judges = userRepository.findAllById(ratings.map { it.judgeId }).associateBy { it.id }

        val criteriaRatings = criteria.map { criterion ->
            val criterionRatings = ratings.filter { it.criteriaId == criterion.id }
            val judgeRatings = criterionRatings.map { rating ->
                val judge = judges[rating.judgeId]!!
                JudgeRating(
                    judgeNickname = judge.nickname,
                    score = rating.score.setScale(2, RoundingMode.HALF_UP).toString(),
                    comment = rating.comment
                )
            }

            val avgScore = if (criterionRatings.isNotEmpty()) {
                criterionRatings.map { it.score }
                    .reduce { acc, score -> acc.add(score) }
                    .divide(BigDecimal(criterionRatings.size), 2, RoundingMode.HALF_UP)
            } else {
                BigDecimal.ZERO
            }

            CriteriaRatingSummary(
                criteriaId = criterion.id!!,
                criteriaName = criterion.name,
                maxScore = criterion.maxScore,
                weight = criterion.weight.toString(),
                averageScore = avgScore.toString(),
                judgeRatings = judgeRatings
            )
        }

        return ProjectRatingSummaryResponse(
            projectId = project.publicId.toString(),
            criteriaRatings = criteriaRatings
        )
    }

    @Transactional(readOnly = true)
    fun getMyRatings(projectId: String, userId: Long): List<MyRatingResponse> {
        val project = projectRepository.findByPublicId(UUID.fromString(projectId))
            ?: throw IllegalArgumentException("Project not found")

        val jam = gameJamRepository.findById(project.jamId).orElseThrow()
        val isJudge = jamJudgeRepository.existsByJamIdAndJudgeId(jam.id!!, userId)

        if (!isJudge) {
            throw IllegalArgumentException("You are not a judge for this jam")
        }

        val ratings = ratingRepository.findAllByProjectIdAndJudgeId(project.id!!, userId)
        val criteriaMap = ratingCriteriaRepository.findAllById(ratings.map { it.criteriaId })
            .associateBy { it.id }

        return ratings.map { rating ->
            val criteria = criteriaMap[rating.criteriaId]!!
            MyRatingResponse(
                id = rating.id!!,
                criteriaId = criteria.id!!,
                criteriaName = criteria.name,
                score = rating.score.setScale(2, RoundingMode.HALF_UP).toString(),
                maxScore = criteria.maxScore,
                comment = rating.comment,
                updatedAt = rating.updatedAt.toString()
            )
        }
    }

    @Transactional(readOnly = true)
    fun getJudgeProgress(jamId: String, userId: Long): JudgeProgressResponse {
        val jam = gameJamRepository.findByPublicId(UUID.fromString(jamId))
            ?: throw IllegalArgumentException("Game jam not found")

        val isJudge = jamJudgeRepository.existsByJamIdAndJudgeId(jam.id!!, userId)
        if (!isJudge) {
            throw IllegalArgumentException("You are not a judge for this jam")
        }

        val projects = projectRepository.findPublishedProjectsByJamId(jam.id!!)
        val criteria = ratingCriteriaRepository.findAllByJamIdOrderByOrderIndex(jam.id!!)
        val allRatings = ratingRepository.findAllByProjectIdIn(projects.map { it.id!! })
            .filter { it.judgeId == userId }

        val ratedProjects = mutableSetOf<Long>()
        val missingProjects = mutableListOf<MissingProjectInfo>()

        projects.forEach { project ->
            val projectRatings = allRatings.filter { it.projectId == project.id }
            val ratedCriteriaIds = projectRatings.map { it.criteriaId }.toSet()
            val missingCriteriaIds = criteria.map { it.id!! }.filter { it !in ratedCriteriaIds }

            if (missingCriteriaIds.isEmpty()) {
                ratedProjects.add(project.id!!)
            } else {
                val team = project.teamId?.let { teamRepository.findById(it).orElse(null) }
                val missingCriteriaNames = criteria
                    .filter { it.id in missingCriteriaIds }
                    .map { it.name }

                missingProjects.add(
                    MissingProjectInfo(
                        projectId = project.publicId.toString(),
                        projectTitle = project.title,
                        teamName = team?.name,
                        missingCriteria = missingCriteriaNames
                    )
                )
            }
        }

        return JudgeProgressResponse(
            jamId = jam.publicId.toString(),
            jamName = jam.name,
            totalProjects = projects.size,
            ratedProjects = ratedProjects.size,
            missingProjects = missingProjects
        )
    }

    @Transactional
    fun validateAndCleanupIncompleteRatings(jamId: Long) {
        val jam = gameJamRepository.findById(jamId).orElseThrow()

        if (jam.status != GameJamStatus.COMPLETED) {
            return
        }

        val judges = jamJudgeRepository.findAllByJamId(jamId).map { it.judgeId }
        val projects = projectRepository.findPublishedProjectsByJamId(jamId)
        val criteriaCount = ratingCriteriaRepository.countByJamId(jamId)

        judges.forEach { judgeId ->
            val judgeHasCompleteRatings = projects.all { project ->
                val ratedCriteria = ratingRepository.countRatedCriteriaByJudgeAndProject(judgeId, project.id!!)
                ratedCriteria == criteriaCount
            }

            if (!judgeHasCompleteRatings) {
                ratingRepository.deleteAllByJudgeId(judgeId)
                jamJudgeRepository.deleteByJamIdAndJudgeId(jamId, judgeId)
            }
        }
    }

    private fun validateScore(score: BigDecimal, maxScore: Int) {
        if (score < BigDecimal.ZERO || score > BigDecimal(maxScore)) {
            throw IllegalArgumentException("Score must be between 0 and $maxScore")
        }

        val scaled = score.multiply(BigDecimal(2)).setScale(0, RoundingMode.HALF_UP)
        if (scaled.remainder(BigDecimal.ONE) != BigDecimal.ZERO) {
            throw IllegalArgumentException("Score must be in increments of 0.5")
        }
    }

    private fun toRatingResponse(
        rating: Rating,
        project: Project,
        judge: User,
        criteria: RatingCriteria
    ): RatingResponse {
        return RatingResponse(
            id = rating.id!!,
            projectId = project.publicId.toString(),
            judgeId = judge.publicId.toString(),
            judgeNickname = judge.nickname,
            criteriaId = criteria.id!!,
            criteriaName = criteria.name,
            score = rating.score.setScale(2, RoundingMode.HALF_UP).toString(),
            maxScore = criteria.maxScore,
            comment = rating.comment,
            createdAt = rating.createdAt.toString(),
            updatedAt = rating.updatedAt.toString()
        )
    }
}