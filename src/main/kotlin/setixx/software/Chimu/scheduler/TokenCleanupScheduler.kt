package setixx.software.Chimu.scheduler

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import setixx.software.Chimu.service.RefreshTokenService

@Component
class TokenCleanupScheduler(
    private val refreshTokenService: RefreshTokenService
) {
    private val logger = LoggerFactory.getLogger(TokenCleanupScheduler::class.java)

    @Scheduled(cron = "0 0 3 * * *")
    fun cleanupExpiredTokens() {
        logger.info("Starting token cleanup task")
        try {
            refreshTokenService.cleanupExpiredTokens()
            logger.info("Token cleanup completed successfully")
        } catch (e: Exception) {
            logger.error("Error during token cleanup", e)
        }
    }
}