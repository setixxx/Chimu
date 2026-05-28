package software.setixx.chimu.api.domain

import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;
import jakarta.persistence.*
import java.time.Instant

/**
 * Сущность токена аутентификации.
 * Используется для хранения хешей Refresh-токенов и отслеживания сессий пользователей.
 */
@Entity
@Table(name = "auth_tokens")
class AuthToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(name = "token_hash", nullable = false, unique = true)
    var tokenHash: String,

    @Column(name = "expires_at", nullable = false)
    var expiresAt: Instant,

    @Generated(event = [EventType.INSERT])
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    var createdAt: Instant? = null,

    @Column(name = "revoked_at")
    var revokedAt: Instant? = null,

    @Column(name = "last_used_at")
    var lastUsedAt: Instant? = null,

    @Column(name = "user_agent", columnDefinition = "TEXT")
    var userAgent: String? = null,

    @Column(name = "ip_address", length = 45)
    var ipAddress: String? = null
) {
    fun isValid(): Boolean {
        return revokedAt == null && expiresAt.isAfter(Instant.now())
    }

    fun revoke() {
        this.revokedAt = Instant.now()
    }

    fun updateLastUsed() {
        this.lastUsedAt = Instant.now()
    }
}