package software.setixx.chimu.api.service

import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import software.setixx.chimu.api.domain.AuthToken
import software.setixx.chimu.api.repository.AuthTokenRepository
import software.setixx.chimu.api.repository.UserRepository
import software.setixx.chimu.api.security.CustomUserDetails
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
        val customUserDetails = userDetails as CustomUserDetails
        val user = userRepository.findByPublicId(customUserDetails.publicId)
            ?: throw IllegalStateException("User not found")

        val tokenHash = hashToken(token)

        val authToken = AuthToken(
            userId = user.id!!,
            tokenHash = tokenHash,
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

        return CustomUserDetails(
            publicId = user.publicId,
            email = user.email,
            passwordHash = user.passwordHash,
            authorities = listOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))
        )
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
        return authTokenRepository.findAllByUserId(userId)
            .filter { it.isValid() }
    }

    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(token.toByteArray())
        return Base64.getEncoder().encodeToString(hashBytes)
    }
}