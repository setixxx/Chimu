package software.setixx.chimu.api.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import software.setixx.chimu.api.domain.GameJam
import software.setixx.chimu.api.domain.GameJamStatus
import software.setixx.chimu.api.domain.RatingCriteria
import software.setixx.chimu.api.domain.User
import software.setixx.chimu.api.domain.UserRole
import software.setixx.chimu.api.dto.CreateGameJamRequest
import software.setixx.chimu.api.dto.CriteriaResponse
import software.setixx.chimu.api.dto.GameJamDetailsResponse
import software.setixx.chimu.api.dto.GameJamResponse
import software.setixx.chimu.api.dto.JudgeResponse
import software.setixx.chimu.api.dto.UpdateGameJamRequest
import software.setixx.chimu.api.repository.GameJamRepository
import software.setixx.chimu.api.repository.JamJudgeRepository
import software.setixx.chimu.api.repository.JamTeamRegistrationRepository
import software.setixx.chimu.api.repository.ProjectRepository
import software.setixx.chimu.api.repository.RatingCriteriaRepository
import software.setixx.chimu.api.repository.UserRepository
import java.util.UUID

@Service
class GameJamService(
    private val gameJamRepository: GameJamRepository,
    private val userRepository: UserRepository,
    private val ratingCriteriaRepository: RatingCriteriaRepository,
    private val jamJudgeRepository: JamJudgeRepository,
    private val registrationRepository: JamTeamRegistrationRepository,
    private val projectRepository: ProjectRepository
) {

    @Transactional
    fun createGameJam(organizerId: Long, request: CreateGameJamRequest): GameJamDetailsResponse {
        val organizer = userRepository.findById(organizerId)
            .orElseThrow { IllegalArgumentException("User not found") }

        if (organizer.role != UserRole.ORGANIZER && organizer.role != UserRole.ADMIN) {
            throw IllegalArgumentException("Only organizers can create game jams")
        }

        validateDates(request.startDate, request.endDate, request.submissionDeadline, request.judgingEndDate)
        validateTeamSizes(request.minTeamSize, request.maxTeamSize)

        val gameJam = GameJam(
            organizerId = organizerId,
            name = request.name,
            description = request.description,
            theme = request.theme,
            rules = request.rules,
            startDate = request.startDate,
            endDate = request.endDate,
            submissionDeadline = request.submissionDeadline,
            judgingEndDate = request.judgingEndDate,
            minTeamSize = request.minTeamSize,
            maxTeamSize = request.maxTeamSize,
            status = GameJamStatus.DRAFT
        )

        val saved = gameJamRepository.save(gameJam)
        return toDetailsResponse(saved, organizer)
    }

    @Transactional(readOnly = true)
    fun getAllGameJams(statusFilter: GameJamStatus?): List<GameJamResponse> {
        val jams = if (statusFilter != null) {
            gameJamRepository.findAllByStatus(statusFilter)
        } else {
            gameJamRepository.findAll()
        }

        return jams.map { jam ->
            val organizer = userRepository.findById(jam.organizerId).orElseThrow()
            val registeredCount = registrationRepository.countRegisteredTeams(jam.id!!).toInt()
            toResponse(jam, organizer, registeredCount)
        }
    }

    @Transactional(readOnly = true)
    fun getGameJamById(jamId: String): GameJamDetailsResponse {
        val jam = gameJamRepository.findByPublicId(UUID.fromString(jamId))
            ?: throw IllegalArgumentException("Game jam not found")

        val organizer = userRepository.findById(jam.organizerId).orElseThrow()
        return toDetailsResponse(jam, organizer)
    }

    @Transactional
    fun updateGameJam(jamId: String, userId: Long, request: UpdateGameJamRequest): GameJamDetailsResponse {
        val jam = gameJamRepository.findByPublicId(UUID.fromString(jamId))
            ?: throw IllegalArgumentException("Game jam not found")

        val user = userRepository.findById(userId).orElseThrow()

        if (jam.organizerId != userId && user.role != UserRole.ADMIN) {
            throw IllegalArgumentException("Only the organizer or admin can update this game jam")
        }

        if (jam.status !in listOf(GameJamStatus.DRAFT, GameJamStatus.ANNOUNCED)) {
            throw IllegalArgumentException("Cannot update game jam after it has started")
        }

        request.name?.let { jam.name = it }
        request.description?.let { jam.description = it }
        request.theme?.let { jam.theme = it }
        request.rules?.let { jam.rules = it }
        request.startDate?.let { jam.startDate = it }
        request.endDate?.let { jam.endDate = it }
        request.submissionDeadline?.let { jam.submissionDeadline = it }
        request.judgingEndDate?.let { jam.judgingEndDate = it }
        request.minTeamSize?.let { jam.minTeamSize = it }
        request.maxTeamSize?.let { jam.maxTeamSize = it }

        validateDates(jam.startDate, jam.endDate, jam.submissionDeadline, jam.judgingEndDate)
        validateTeamSizes(jam.minTeamSize, jam.maxTeamSize)

        gameJamRepository.save(jam)

        val organizer = userRepository.findById(jam.organizerId).orElseThrow()
        return toDetailsResponse(jam, organizer)
    }

    @Transactional
    fun changeGameJamStatus(jamId: String, userId: Long, newStatus: GameJamStatus): GameJamDetailsResponse {
        val jam = gameJamRepository.findByPublicId(UUID.fromString(jamId))
            ?: throw IllegalArgumentException("Game jam not found")

        val user = userRepository.findById(userId).orElseThrow()

        if (jam.organizerId != userId && user.role != UserRole.ADMIN) {
            throw IllegalArgumentException("Only the organizer or admin can change game jam status")
        }

        validateStatusTransition(jam.status, newStatus)

        jam.status = newStatus
        gameJamRepository.save(jam)

        val organizer = userRepository.findById(jam.organizerId).orElseThrow()
        return toDetailsResponse(jam, organizer)
    }

    @Transactional
    fun deleteGameJam(jamId: String, userId: Long) {
        val jam = gameJamRepository.findByPublicId(UUID.fromString(jamId))
            ?: throw IllegalArgumentException("Game jam not found")

        val user = userRepository.findById(userId).orElseThrow()

        if (jam.organizerId != userId && user.role != UserRole.ADMIN) {
            throw IllegalArgumentException("Only the organizer or admin can delete this game jam")
        }

        if (jam.status !in listOf(GameJamStatus.DRAFT, GameJamStatus.ANNOUNCED)) {
            throw IllegalArgumentException("Cannot delete game jam after it has started")
        }

        gameJamRepository.delete(jam)
    }

    private fun validateDates(start: java.time.Instant, end: java.time.Instant,
                              submission: java.time.Instant, judging: java.time.Instant?) {
        if (start >= end) {
            throw IllegalArgumentException("Start date must be before end date")
        }
        if (submission < end) {
            throw IllegalArgumentException("Submission deadline must be after or equal to end date")
        }
        if (judging != null && judging < submission) {
            throw IllegalArgumentException("Judging end date must be after submission deadline")
        }
    }

    private fun validateTeamSizes(min: Int, max: Int) {
        if (min < 1) {
            throw IllegalArgumentException("Minimum team size must be at least 1")
        }
        if (min > max) {
            throw IllegalArgumentException("Minimum team size must not exceed maximum team size")
        }
    }

    private fun validateStatusTransition(current: GameJamStatus, new: GameJamStatus) {
        val validTransitions = mapOf(
            GameJamStatus.DRAFT to listOf(GameJamStatus.ANNOUNCED, GameJamStatus.CANCELLED),
            GameJamStatus.ANNOUNCED to listOf(GameJamStatus.IN_PROGRESS, GameJamStatus.CANCELLED),
            GameJamStatus.IN_PROGRESS to listOf(GameJamStatus.JUDGING, GameJamStatus.CANCELLED),
            GameJamStatus.JUDGING to listOf(GameJamStatus.COMPLETED, GameJamStatus.CANCELLED),
            GameJamStatus.COMPLETED to emptyList(),
            GameJamStatus.CANCELLED to emptyList()
        )

        if (new !in validTransitions[current].orEmpty()) {
            throw IllegalArgumentException("Invalid status transition from $current to $new")
        }
    }

    private fun toResponse(jam: GameJam, organizer: User, registeredCount: Int): GameJamResponse {
        return GameJamResponse(
            id = jam.publicId.toString(),
            name = jam.name,
            description = jam.description,
            theme = jam.theme,
            startDate = jam.startDate.toString(),
            endDate = jam.endDate.toString(),
            submissionDeadline = jam.submissionDeadline.toString(),
            status = jam.status,
            organizerId = organizer.publicId.toString(),
            organizerNickname = organizer.nickname,
            registeredTeamsCount = registeredCount,
            maxTeamSize = jam.maxTeamSize,
            minTeamSize = jam.minTeamSize,
            createdAt = jam.createdAt.toString()
        )
    }

    private fun toDetailsResponse(jam: GameJam, organizer: User): GameJamDetailsResponse {
        val criteria = ratingCriteriaRepository.findAllByJamIdOrderByOrderIndex(jam.id!!)
        val judges = jamJudgeRepository.findAllByJamId(jam.id!!)
        val registeredCount = registrationRepository.countRegisteredTeams(jam.id!!).toInt()
        val submittedCount = projectRepository.countSubmittedProjects(jam.id!!).toInt()

        val judgeUsers = userRepository.findAllById(judges.map { it.judgeId })
            .associateBy { it.id }

        val judgeResponses = judges.map { judge ->
            val user = judgeUsers[judge.judgeId]!!
            JudgeResponse(
                userId = user.publicId.toString(),
                nickname = user.nickname,
                avatarUrl = user.avatarUrl,
                assignedAt = judge.assignedAt.toString()
            )
        }

        return GameJamDetailsResponse(
            id = jam.publicId.toString(),
            name = jam.name,
            description = jam.description,
            theme = jam.theme,
            rules = jam.rules,
            startDate = jam.startDate.toString(),
            endDate = jam.endDate.toString(),
            submissionDeadline = jam.submissionDeadline.toString(),
            judgingEndDate = jam.judgingEndDate?.toString(),
            status = jam.status,
            organizerId = organizer.publicId.toString(),
            organizerNickname = organizer.nickname,
            minTeamSize = jam.minTeamSize,
            maxTeamSize = jam.maxTeamSize,
            createdAt = jam.createdAt.toString(),
            updatedAt = jam.updatedAt.toString(),
            criteria = criteria.map { toCriteriaResponse(it) },
            judges = judgeResponses,
            registeredTeamsCount = registeredCount,
            submittedProjectsCount = submittedCount
        )
    }

    private fun toCriteriaResponse(criteria: RatingCriteria): CriteriaResponse {
        return CriteriaResponse(
            id = criteria.id!!,
            name = criteria.name,
            description = criteria.description,
            maxScore = criteria.maxScore,
            weight = criteria.weight.toString(),
            orderIndex = criteria.orderIndex
        )
    }
}