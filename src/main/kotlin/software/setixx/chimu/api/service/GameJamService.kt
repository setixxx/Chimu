package software.setixx.chimu.api.service

import jakarta.persistence.OptimisticLockException
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
import java.time.Duration
import java.time.Instant
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

        if (gameJamRepository.existsByName(request.name)) {
            throw IllegalArgumentException("Game jam with name '${request.name}' already exists")
        }

        validateDates(
            request.registrationStart,
            request.registrationEnd,
            request.jamStart,
            request.jamEnd,
            request.judgingStart,
            request.judgingEnd
        )
        validateTeamSizes(request.minTeamSize, request.maxTeamSize)

        val gameJam = GameJam(
            organizerId = organizerId,
            name = request.name,
            description = request.description,
            theme = request.theme,
            rules = request.rules,
            registrationStart = request.registrationStart,
            registrationEnd = request.registrationEnd,
            jamStart = request.jamStart,
            jamEnd = request.jamEnd,
            judgingStart = request.judgingStart,
            judgingEnd = request.judgingEnd,
            minTeamSize = request.minTeamSize,
            maxTeamSize = request.maxTeamSize,
            status = GameJamStatus.REGISTRATION_OPEN
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

        if (jam.status !in listOf(GameJamStatus.REGISTRATION_OPEN, GameJamStatus.REGISTRATION_CLOSED)) {
            throw IllegalArgumentException("Cannot update game jam after it has started")
        }

        request.name?.let { newName ->
            if (newName != jam.name) {
                if (gameJamRepository.existsByName(newName)) {
                    throw IllegalArgumentException("Game jam with name '$newName' already exists")
                }
                jam.name = newName
            }
        }
        request.description?.let { jam.description = it }
        request.theme?.let { jam.theme = it }
        request.rules?.let { jam.rules = it }
        request.registrationStart?.let { jam.registrationStart = it }
        request.registrationEnd?.let { jam.registrationEnd = it }
        request.jamStart?.let { jam.jamStart = it }
        request.jamEnd?.let { jam.jamEnd = it }
        request.judgingStart?.let { jam.judgingStart = it }
        request.judgingEnd?.let { jam.judgingEnd = it }
        request.minTeamSize?.let { jam.minTeamSize = it }
        request.maxTeamSize?.let { jam.maxTeamSize = it }

        validateDates(
            jam.registrationStart,
            jam.registrationEnd,
            jam.jamStart,
            jam.jamEnd,
            jam.judgingStart,
            jam.judgingEnd
        )
        validateTeamSizes(jam.minTeamSize, jam.maxTeamSize)

        try {
            gameJamRepository.save(jam)
        } catch (e: OptimisticLockException) {
            throw IllegalStateException("Game jam was modified by another user. Please refresh and try again.")
        }

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

        if (jam.status !in listOf(GameJamStatus.REGISTRATION_OPEN, GameJamStatus.REGISTRATION_CLOSED)) {
            throw IllegalArgumentException("Cannot delete game jam after it has started")
        }

        gameJamRepository.delete(jam)
    }

    private fun validateDates(
        registrationStart: Instant,
        registrationEnd: Instant,
        jamStart: Instant,
        jamEnd: Instant,
        judgingStart: Instant,
        judgingEnd: Instant
    ) {
        if (registrationStart >= registrationEnd) {
            throw IllegalArgumentException("Registration start must be before registration end")
        }
        if (registrationEnd > jamStart) {
            throw IllegalArgumentException("Registration end must be before or equal to jam start")
        }
        if (jamStart >= jamEnd) {
            throw IllegalArgumentException("Jam start must be before jam end")
        }
        if (jamEnd > judgingStart) {
            throw IllegalArgumentException("Jam end must be before or equal to judging start")
        }
        if (judgingStart >= judgingEnd) {
            throw IllegalArgumentException("Judging start must be before judging end")
        }

        val registrationDuration = Duration.between(registrationStart, registrationEnd)
        if (registrationDuration.toMinutes() < 30) {
            throw IllegalArgumentException("Registration period must be at least 30 minutes")
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
            GameJamStatus.REGISTRATION_OPEN to listOf(
                GameJamStatus.REGISTRATION_CLOSED,
                GameJamStatus.CANCELLED
            ),
            GameJamStatus.REGISTRATION_CLOSED to listOf(
                GameJamStatus.IN_PROGRESS,
                GameJamStatus.CANCELLED
            ),
            GameJamStatus.IN_PROGRESS to listOf(
                GameJamStatus.JUDGING,
                GameJamStatus.CANCELLED
            ),
            GameJamStatus.JUDGING to listOf(
                GameJamStatus.COMPLETED,
                GameJamStatus.CANCELLED
            ),
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
            registrationStart = jam.registrationStart.toString(),
            registrationEnd = jam.registrationEnd.toString(),
            jamStart = jam.jamStart.toString(),
            jamEnd = jam.jamEnd.toString(),
            judgingStart = jam.judgingStart.toString(),
            judgingEnd = jam.judgingEnd.toString(),
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
            registrationStart = jam.registrationStart.toString(),
            registrationEnd = jam.registrationEnd.toString(),
            jamStart = jam.jamStart.toString(),
            jamEnd = jam.jamEnd.toString(),
            judgingStart = jam.judgingStart.toString(),
            judgingEnd = jam.judgingEnd.toString(),
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