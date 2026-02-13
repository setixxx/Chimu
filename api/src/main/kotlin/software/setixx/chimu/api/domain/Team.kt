package software.setixx.chimu.api.domain

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "teams",
    uniqueConstraints = [
        UniqueConstraint(name = "uq_teams_public_id", columnNames = ["public_id"]),
        UniqueConstraint(name = "uq_teams_invite_token", columnNames = ["invite_token"])
    ]
)
class Team(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "public_id", nullable = false, columnDefinition = "uuid")
    var publicId: UUID = UUID.randomUUID(),

    @Column(nullable = false, length = 100)
    var name: String,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(name = "leader_id", nullable = false)
    var leaderId: Long,

    @Column(name = "invite_token", nullable = false, length = 64)
    var inviteToken: String,

    @Version
    @Column(nullable = false)
    var version: Long? = null,

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    var createdAt: Instant? = null
) {
    @PrePersist
    fun prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now()
        }
    }
}