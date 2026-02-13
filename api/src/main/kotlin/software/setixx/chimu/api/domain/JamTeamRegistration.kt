package software.setixx.chimu.api.domain

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant

@Entity
@Table(
    name = "jam_team_registrations",
    uniqueConstraints = [
        UniqueConstraint(name = "uq_registrations_jam_team", columnNames = ["jam_id", "team_id"])
    ],
    indexes = [
        Index(name = "idx_registrations_jam_id", columnList = "jam_id"),
        Index(name = "idx_registrations_team_id", columnList = "team_id"),
        Index(name = "idx_registrations_status", columnList = "status"),
        Index(name = "idx_registrations_registered_by", columnList = "registered_by")
    ]
)
class JamTeamRegistration(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "jam_id", nullable = false)
    var jamId: Long,

    @Column(name = "team_id", nullable = false)
    var teamId: Long,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "registration_status")
    var status: RegistrationStatus = RegistrationStatus.PENDING,

    @Column(name = "registered_at", nullable = false, insertable = false, updatable = false)
    var registeredAt: Instant? = null,

    @Column(name = "registered_by", nullable = false)
    var registeredBy: Long,

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    var updatedAt: Instant? = null
)

enum class RegistrationStatus {
    PENDING,
    APPROVED,
    REJECTED,
    WITHDRAWN
}