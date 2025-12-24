package software.setixx.chimu.api.scheduler

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import software.setixx.chimu.api.domain.GameJamStatus
import software.setixx.chimu.api.domain.RegistrationStatus
import software.setixx.chimu.api.repository.GameJamRepository
import software.setixx.chimu.api.repository.JamTeamRegistrationRepository
import software.setixx.chimu.api.repository.TeamMemberRepository

@Component
class JamStatusScheduler(
    private val gameJamRepository: GameJamRepository,
    private val registrationRepository: JamTeamRegistrationRepository,
    private val teamMemberRepository: TeamMemberRepository
) {
    private val logger = LoggerFactory.getLogger(JamStatusScheduler::class.java)

    @Scheduled(cron = "0 */5 * * * *")
    @Transactional
    fun updateJamStatuses() {
        logger.info("Starting automatic jam status update")

        try {
            var totalUpdated = 0

            val jamsToCloseRegistration = gameJamRepository.findJamsToCloseRegistration()
            jamsToCloseRegistration.forEach { jam ->
                val pendingRegistrations = registrationRepository.findAllByJamIdAndStatus(
                    jam.id!!,
                    RegistrationStatus.PENDING
                )

                pendingRegistrations.forEach { registration ->
                    registration.status = RegistrationStatus.REJECTED
                    registrationRepository.save(registration)
                    logger.info("Auto-rejected pending registration for team ${registration.teamId} in jam ${jam.name}")
                }

                jam.status = GameJamStatus.REGISTRATION_CLOSED
                gameJamRepository.save(jam)
                logger.info("Changed jam ${jam.name} (${jam.publicId}) status to REGISTRATION_CLOSED, rejected ${pendingRegistrations.size} pending registrations")
            }
            totalUpdated += jamsToCloseRegistration.size

            val jamsToStart = gameJamRepository.findJamsToStart()
            jamsToStart.forEach { jam ->
                val approvedRegistrations = registrationRepository.findAllByJamIdAndStatus(
                    jam.id!!,
                    RegistrationStatus.APPROVED
                )

                val invalidTeams = mutableListOf<Long>()
                approvedRegistrations.forEach { registration ->
                    val teamSize = teamMemberRepository.countByTeamId(registration.teamId).toInt()
                    if (teamSize < jam.minTeamSize || teamSize > jam.maxTeamSize) {
                        registration.status = RegistrationStatus.REJECTED
                        registrationRepository.save(registration)
                        invalidTeams.add(registration.teamId)
                        logger.warn("Team ${registration.teamId} rejected from jam ${jam.name} due to invalid size: $teamSize (required: ${jam.minTeamSize}-${jam.maxTeamSize})")
                    }
                }

                jam.status = GameJamStatus.IN_PROGRESS
                gameJamRepository.save(jam)
                logger.info("Changed jam ${jam.name} (${jam.publicId}) status to IN_PROGRESS, rejected ${invalidTeams.size} teams with invalid size")
            }
            totalUpdated += jamsToStart.size

            val jamsToJudge = gameJamRepository.findJamsToStartJudging()
            jamsToJudge.forEach { jam ->
                jam.status = GameJamStatus.JUDGING
                gameJamRepository.save(jam)
                logger.info("Changed jam ${jam.name} (${jam.publicId}) status to JUDGING")
            }
            totalUpdated += jamsToJudge.size

            val jamsToComplete = gameJamRepository.findJamsToComplete()
            jamsToComplete.forEach { jam ->
                jam.status = GameJamStatus.COMPLETED
                gameJamRepository.save(jam)
                logger.info("Changed jam ${jam.name} (${jam.publicId}) status to COMPLETED")
            }
            totalUpdated += jamsToComplete.size

            logger.info(
                "Automatic jam status update completed. Total updated: $totalUpdated " +
                        "(Closed registration: ${jamsToCloseRegistration.size}, " +
                        "Started: ${jamsToStart.size}, " +
                        "Judging: ${jamsToJudge.size}, " +
                        "Completed: ${jamsToComplete.size})"
            )
        } catch (e: Exception) {
            logger.error("Error during automatic jam status update", e)
        }
    }
}