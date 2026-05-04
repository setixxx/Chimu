package software.setixx.chimu.api.domain

import jakarta.persistence.*
import org.hibernate.annotations.Generated
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.generator.EventType
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "game_jams")
class GameJam(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "public_id", nullable = false, columnDefinition = "uuid")
    var publicId: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    var organizer: User,

    @Column(nullable = false, length = 200)
    var name: String,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(name = "banner_url", nullable = false, columnDefinition = "TEXT")
    var bannerUrl: String,

    @Column(length = 200)
    var theme: String? = null,

    @Column(columnDefinition = "TEXT")
    var rules: String? = null,

    @Column(name = "registration_start", nullable = false)
    var registrationStart: Instant,

    @Column(name = "registration_end", nullable = false)
    var registrationEnd: Instant,

    @Column(name = "jam_start", nullable = false)
    var jamStart: Instant,

    @Column(name = "jam_end", nullable = false)
    var jamEnd: Instant,

    @Column(name = "judging_start", nullable = false)
    var judgingStart: Instant,

    @Column(name = "judging_end", nullable = false)
    var judgingEnd: Instant,

    @Column(name = "max_team_size", nullable = false)
    var maxTeamSize: Int = 10,

    @Column(name = "min_team_size", nullable = false)
    var minTeamSize: Int = 1,

    @Column(nullable = false, columnDefinition = "game_jam_status")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    var status: GameJamStatus = GameJamStatus.REGISTRATION_OPEN,

    @Version
    @Column(nullable = false)
    var version: Long? = null,

    @Generated(event = [EventType.INSERT])
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    var createdAt: Instant? = null,

    @Generated(event = [EventType.INSERT, EventType.UPDATE])
    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    var updatedAt: Instant? = null,

    @Column(name = "deleted_at")
    var deletedAt: Instant? = null,
)