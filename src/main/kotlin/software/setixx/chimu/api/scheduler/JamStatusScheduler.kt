package software.setixx.chimu.api.scheduler

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import software.setixx.chimu.api.domain.GameJamStatus
import software.setixx.chimu.api.repository.GameJamRepository

@Component
class JamStatusScheduler(
    private val gameJamRepository: GameJamRepository
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
                jam.status = GameJamStatus.REGISTRATION_CLOSED
                gameJamRepository.save(jam)
                logger.info("Changed jam ${jam.name} (${jam.publicId}) status to REGISTRATION_CLOSED")
            }
            totalUpdated += jamsToCloseRegistration.size

            val jamsToStart = gameJamRepository.findJamsToStart()
            jamsToStart.forEach { jam ->
                jam.status = GameJamStatus.IN_PROGRESS
                gameJamRepository.save(jam)
                logger.info("Changed jam ${jam.name} (${jam.publicId}) status to IN_PROGRESS")
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