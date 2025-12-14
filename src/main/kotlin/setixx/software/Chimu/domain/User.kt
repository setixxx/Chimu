package setixx.software.Chimu.domain

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "users",
    uniqueConstraints = [
        UniqueConstraint(name = "uq_users_public_id", columnNames = ["public_id"]),
        UniqueConstraint(name = "uq_users_email", columnNames = ["email"]),
        UniqueConstraint(name = "uq_users_nickname", columnNames = ["nickname"])
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

    @Column(name = "first_name", length = 100)
    var firstName: String? = null,

    @Column(name = "last_name", length = 100)
    var lastName: String? = null,

    @Column(name = "nickname", length = 50, nullable = false)
    var nickname: String,

    @Column(name = "bio", columnDefinition = "text")
    var bio: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "primary_role")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    var primaryRole: UserPrimaryRole? = null,

    @Column(name = "github_url")
    var githubUrl: String? = null,

    @Column(name = "telegram_username")
    var telegramUsername: String? = null,

    @Column(name = "avatar_url")
    var avatarUrl: String? = null,

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    var createdAt: Instant? = null,

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    var updatedAt: Instant? = null,

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_skills",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "skill_id")]
    )
    var skills: MutableSet<Skill> = mutableSetOf()
)

enum class UserRole {
    PARTICIPANT,
    ORGANIZER,
    JUDGE,
    ADMIN,
    GUEST
}