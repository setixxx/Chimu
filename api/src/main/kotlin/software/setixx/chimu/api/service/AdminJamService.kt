package software.setixx.chimu.api.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import software.setixx.chimu.api.domain.GameJamStatus
import software.setixx.chimu.api.domain.RegistrationStatus
import software.setixx.chimu.api.domain.User
import software.setixx.chimu.api.dto.GameJamDetailsResponse
import software.setixx.chimu.api.repository.GameJamRepository
import software.setixx.chimu.api.repository.JamTeamRegistrationRepository
import software.setixx.chimu.api.repository.TeamMemberRepository
import java.util.UUID

@Service
class AdminJamService(
    private val gameJamRepository: GameJamRepository,
    private val registrationRepository: JamTeamRegistrationRepository,
    private val teamMemberRepository: TeamMemberRepository,
    private val ratingService: RatingService,
    private val jamSchedulerService: JamSchedulerService,
    private val gameJamService: GameJamService
) {
    private val log = LoggerFactory.getLogger(AdminJamService::class.java)

    @Transactional
    fun forceStatus(jamId: String, user: User, targetStatus: GameJamStatus): GameJamDetailsResponse {
        val jam = gameJamRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(jamId))
            ?: throw IllegalArgumentException("Game jam not found")

        val forbidden = setOf(GameJamStatus.CANCELLED, GameJamStatus.COMPLETED)
        if (jam.status in forbidden) {
            throw IllegalArgumentException("Cannot force-transition from ${jam.status}")
        }

        log.warn("ADMIN force-transition jam $jamId: ${jam.status} -> $targetStatus")

        when (targetStatus) {
            GameJamStatus.REGISTRATION_CLOSED -> {
                val pending = registrationRepository.findAllByGameJamIdAndStatusAndDeletedAtIsNull(
                    jam.id!!, RegistrationStatus.PENDING
                )
                pending.forEach { it.status = RegistrationStatus.REJECTED }
                registrationRepository.saveAll(pending)
            }
            GameJamStatus.IN_PROGRESS -> {
                val approved = registrationRepository.findAllByGameJamIdAndStatusAndDeletedAtIsNull(
                    jam.id!!, RegistrationStatus.APPROVED
                )
                approved.forEach { reg ->
                    val size = teamMemberRepository.countByTeamId(reg.team.id!!).toInt()
                    if (size < jam.minTeamSize || size > jam.maxTeamSize) {
                        reg.status = RegistrationStatus.REJECTED
                        registrationRepository.save(reg)
                    }
                }
            }
            GameJamStatus.COMPLETED -> {
                ratingService.validateAndCleanupIncompleteRatings(jam.id!!)
            }
            else -> {}
        }

        jam.status = targetStatus
        gameJamRepository.save(jam)
        jamSchedulerService.cancelExisting(jamId)
        log.warn("ADMIN force-transition complete: jam $jamId is now $targetStatus")

        return gameJamService.toDetailsResponse(jam, jam.organizer, user.id, user.role)
    }
}