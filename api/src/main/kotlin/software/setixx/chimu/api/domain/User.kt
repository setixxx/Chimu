package software.setixx.chimu.api.domain

import jakarta.persistence.*
import org.hibernate.annotations.Generated
import org.hibernate.generator.EventType
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "users")
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialization_id")
    var specialization: Specialization? = null,

    @Column(name = "github_url")
    var githubUrl: String? = null,

    @Column(name = "telegram_username")
    var telegramUsername: String? = null,

    @Column(name = "avatar_url")
    var avatarUrl: String? = null,

    @Generated(event = [EventType.INSERT])
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    var createdAt: Instant? = null,

    @Generated(event = [EventType.INSERT, EventType.UPDATE])
    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    var updatedAt: Instant? = null,

    @Column(name = "deleted_at")
    var deletedAt: Instant? = null,

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    var teamMemberships: MutableSet<TeamMember> = mutableSetOf(),

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    var skills: MutableSet<UserSkill> = mutableSetOf()
)