package software.setixx.chimu.api.domain

import jakarta.persistence.*
import org.hibernate.annotations.Generated
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.generator.EventType
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "jam_team_registrations")
class JamTeamRegistration(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "public_id", nullable = false, columnDefinition = "uuid")
    var publicId: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jam_id", nullable = false)
    val gameJam: GameJam,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    val team: Team,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "registration_status")
    var status: RegistrationStatus = RegistrationStatus.PENDING,

    @Generated(event = [EventType.INSERT])
    @Column(name = "registered_at", nullable = false, insertable = false, updatable = false)
    var registeredAt: Instant? = null,

    @Column(name = "registered_by", nullable = false)
    var registeredBy: Long,

    @Generated(event = [EventType.INSERT, EventType.UPDATE])
    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    var updatedAt: Instant? = null,

    @Column(name = "deleted_at")
    var deletedAt: Instant? = null
)