package setixx.software.Chimu.domain

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "auth_tokens",
    uniqueConstraints = [
        UniqueConstraint(name = "uq_auth_tokens_token_hash", columnNames = ["token_hash"])
    ],
    indexes = [
        Index(name = "idx_auth_tokens_user_id", columnList = "user_id"),
        Index(name = "idx_auth_tokens_token_hash", columnList = "token_hash"),
        Index(name = "idx_auth_tokens_expires_at", columnList = "expires_at")
    ]
)
class AuthToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "user_id", nullable = false)
    var userId: Long,

    @Column(name = "token_hash", nullable = false, unique = true)
    var tokenHash: String,

    @Column(name = "expires_at", nullable = false)
    var expiresAt: Instant,

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