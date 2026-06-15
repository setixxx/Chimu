package software.setixx.chimu.api.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import software.setixx.chimu.api.domain.GameJamStatus
import software.setixx.chimu.api.domain.RegistrationStatus
import software.setixx.chimu.api.repository.GameJamRepository
import software.setixx.chimu.api.repository.JamTeamRegistrationRepository
import software.setixx.chimu.api.repository.TeamMemberRepository
import java.util.UUID

@Component
class JamTransitionExecutor(
    private val gameJamRepository: GameJamRepository,
    private val registrationRepository: JamTeamRegistrationRepository,
    private val teamMemberRepository: TeamMemberRepository,
    private val ratingService: RatingService
) {
    private val log = LoggerFactory.getLogger(JamTransitionExecutor::class.java)

    @Transactional
    fun execute(jamId: String, targetStatus: GameJamStatus) {
        val jam = gameJamRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(jamId))
            ?: run {
                log.warn("Jam $jamId not found during transition to $targetStatus")
                return
            }

        if (jam.status == GameJamStatus.CANCELLED || jam.status == GameJamStatus.COMPLETED) {
            log.info("Jam $jamId is ${jam.status}, skipping transition to $targetStatus")
            return
        }

        log.info("Transitioning jam $jamId: ${jam.status} -> $targetStatus")

        when (targetStatus) {
            GameJamStatus.REGISTRATION_CLOSED -> {
                val pending = registrationRepository.findAllByGameJamIdAndStatusAndDeletedAtIsNull(
                    jam.id!!, RegistrationStatus.PENDING
                )
                pending.forEach { it.status = RegistrationStatus.REJECTED }
                registrationRepository.saveAll(pending)
                log.info("Auto-rejected ${pending.size} pending registrations for jam ${jam.publicId}")
            }
            GameJamStatus.IN_PROGRESS -> {
                val approved = registrationRepository.findAllByGameJamIdAndStatusAndDeletedAtIsNull(
                    jam.id!!, RegistrationStatus.APPROVED
                )
                var rejected = 0
                approved.forEach { reg ->
                    val size = teamMemberRepository.countByTeamId(reg.team.id!!).toInt()
                    if (size < jam.minTeamSize || size > jam.maxTeamSize) {
                        reg.status = RegistrationStatus.REJECTED
                        registrationRepository.save(reg)
                        rejected++
                    }
                }
                log.info("Jam ${jam.publicId} started, rejected $rejected teams due to invalid size")
            }
            GameJamStatus.JUDGING -> {
                log.info("Jam ${jam.publicId} entering JUDGING phase")
            }
            GameJamStatus.COMPLETED -> {
                ratingService.validateAndCleanupIncompleteRatings(jam.id!!)
                log.info("Jam ${jam.publicId} completed")
            }
            else -> {}
        }

        jam.status = targetStatus
        gameJamRepository.save(jam)
        log.info("Jam $jamId successfully transitioned to $targetStatus")
    }
}