package setixx.software.Chimu.service

import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import setixx.software.Chimu.domain.AuthToken
import setixx.software.Chimu.domain.TokenType
import setixx.software.Chimu.domain.User
import setixx.software.Chimu.repository.AuthTokenRepository
import setixx.software.Chimu.repository.UserRepository
import java.security.MessageDigest
import java.time.Instant
import java.util.*

@Service
class RefreshTokenService(
    private val authTokenRepository: AuthTokenRepository,
    private val userRepository: UserRepository
) {

    @Transactional
    fun saveRefreshToken(
        token: String,
        userDetails: UserDetails,
        expiresAt: Instant,
        request: HttpServletRequest? = null
    ) {
        val user = userRepository.findByEmail(userDetails.username)
            ?: throw IllegalStateException("User not found")

        val tokenHash = hashToken(token)

        val authToken = AuthToken(
            userId = user.id!!,
            tokenHash = tokenHash,
            tokenType = TokenType.REFRESH,
            expiresAt = expiresAt,
            userAgent = request?.getHeader("User-Agent"),
            ipAddress = request?.remoteAddr
        )

        authTokenRepository.save(authToken)
    }

    @Transactional(readOnly = true)
    fun findUserDetailsByToken(token: String): UserDetails? {
        val tokenHash = hashToken(token)
        val authToken = authTokenRepository.findValidTokenByHash(tokenHash) ?: return null

        val user = userRepository.findById(authToken.userId).orElse(null) ?: return null

        return org.springframework.security.core.userdetails.User.builder()
            .username(user.email)
            .password(user.passwordHash)
            .roles(user.role.name)
            .build()
    }

    @Transactional
    fun updateLastUsed(token: String) {
        val tokenHash = hashToken(token)
        val authToken = authTokenRepository.findByTokenHash(tokenHash)
        authToken?.updateLastUsed()
    }

    @Transactional
    fun revokeToken(token: String) {
        val tokenHash = hashToken(token)
        authTokenRepository.revokeToken(tokenHash)
    }

    @Transactional
    fun revokeAllUserTokens(userId: Long) {
        authTokenRepository.revokeAllUserTokens(userId)
    }

    @Transactional
    fun cleanupExpiredTokens() {
        authTokenRepository.deleteExpiredTokens()

        val thirtyDaysAgo = Instant.now().minusSeconds(30 * 24 * 60 * 60)
        authTokenRepository.deleteRevokedTokens(thirtyDaysAgo)
    }

    @Transactional(readOnly = true)
    fun getUserActiveSessions(userId: Long): List<AuthToken> {
        return authTokenRepository.findAllByUserIdAndTokenType(userId, TokenType.REFRESH)
            .filter { it.isValid() }
    }

    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(token.toByteArray())
        return Base64.getEncoder().encodeToString(hashBytes)
    }
}