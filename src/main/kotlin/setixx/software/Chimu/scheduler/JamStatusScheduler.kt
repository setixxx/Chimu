package setixx.software.Chimu.scheduler

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import setixx.software.Chimu.domain.GameJamStatus
import setixx.software.Chimu.repository.GameJamRepository

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
            val jamsToStart = gameJamRepository.findJamsToStart()
            jamsToStart.forEach { jam ->
                jam.status = GameJamStatus.IN_PROGRESS
                gameJamRepository.save(jam)
                logger.info("Changed jam ${jam.name} (${jam.publicId}) status to IN_PROGRESS")
            }

            val jamsToJudge = gameJamRepository.findJamsToStartJudging()
            jamsToJudge.forEach { jam ->
                jam.status = GameJamStatus.JUDGING
                gameJamRepository.save(jam)
                logger.info("Changed jam ${jam.name} (${jam.publicId}) status to JUDGING")
            }

            logger.info("Automatic jam status update completed. Started: ${jamsToStart.size}, Judging: ${jamsToJudge.size}")
        } catch (e: Exception) {
            logger.error("Error during automatic jam status update", e)
        }
    }
}