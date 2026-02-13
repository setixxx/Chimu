package software.setixx.chimu.api.domain

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "team_members",
    uniqueConstraints = [
        UniqueConstraint(name = "uq_team_members_team_user", columnNames = ["team_id", "user_id"])
    ],
    indexes = [
        Index(name = "idx_team_members_team_id", columnList = "team_id"),
        Index(name = "idx_team_members_user_id", columnList = "user_id"),
        Index(name = "idx_team_members_specialization_id", columnList = "specialization_id")
    ]
)
class TeamMember(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "team_id", nullable = false)
    var teamId: Long,

    @Column(name = "user_id", nullable = false)
    var userId: Long,

    @Column(name = "specialization_id")
    var specializationId: Long? = null,

    @Column(name = "joined_at", nullable = false, insertable = false, updatable = false)
    var joinedAt: Instant? = null
) {
    @PrePersist
    fun prePersist() {
        if (joinedAt == null) {
            joinedAt = Instant.now()
        }
    }
}