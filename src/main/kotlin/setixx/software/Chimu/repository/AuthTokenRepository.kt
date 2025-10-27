package setixx.software.Chimu.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import setixx.software.Chimu.domain.AuthToken
import setixx.software.Chimu.domain.TokenType
import java.time.Instant

interface AuthTokenRepository : JpaRepository<AuthToken, Long> {
    fun findByTokenHash(tokenHash: String): AuthToken?

    fun findAllByUserId(userId: Long): List<AuthToken>

    fun findAllByUserIdAndTokenType(userId: Long, tokenType: TokenType): List<AuthToken>

    @Query("""
        SELECT t FROM AuthToken t 
        WHERE t.tokenHash = :tokenHash 
        AND t.revokedAt IS NULL 
        AND t.expiresAt > :now
    """)
    fun findValidTokenByHash(
        @Param("tokenHash") tokenHash: String,
        @Param("now") now: Instant = Instant.now()
    ): AuthToken?

    @Modifying
    @Query("""
        UPDATE AuthToken t 
        SET t.revokedAt = :now 
        WHERE t.userId = :userId 
        AND t.revokedAt IS NULL
    """)
    fun revokeAllUserTokens(
        @Param("userId") userId: Long,
        @Param("now") now: Instant = Instant.now()
    ): Int

    @Modifying
    @Query("""
        UPDATE AuthToken t 
        SET t.revokedAt = :now 
        WHERE t.tokenHash = :tokenHash
    """)
    fun revokeToken(
        @Param("tokenHash") tokenHash: String,
        @Param("now") now: Instant = Instant.now()
    ): Int

    @Modifying
    @Query("DELETE FROM AuthToken t WHERE t.expiresAt < :before")
    fun deleteExpiredTokens(@Param("before") before: Instant = Instant.now()): Int

    @Modifying
    @Query("DELETE FROM AuthToken t WHERE t.revokedAt < :before")
    fun deleteRevokedTokens(@Param("before") before: Instant): Int
}