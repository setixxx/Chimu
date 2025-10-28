package setixx.software.Chimu.domain

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "users",
    uniqueConstraints = [
        UniqueConstraint(name = "uq_users_public_id", columnNames = ["public_id"]),
        UniqueConstraint(name = "uq_users_email", columnNames = ["email"])
    ]
)
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "public_id", nullable = false, columnDefinition = "uuid")
    var publicId: UUID = UUID.randomUUID(),

    @Column(name = "email", nullable = false, columnDefinition = "citext")
    var email: String,

    @Column(name = "password_hash", nullable = false)
    var passwordHash: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    var role: UserRole = UserRole.PARTICIPANT,

    @Column(name = "display_name", length = 120, nullable = false)
    var displayName: String,

    @Column(name = "avatar_url")
    var avatarUrl: String? = null,

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    var createdAt: Instant,

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    var updatedAt: Instant? = null
)

enum class UserRole {
    PARTICIPANT,
    ORGANIZER,
    JUDGE,
    ADMIN,
    GUEST
}