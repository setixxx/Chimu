package software.setixx.chimu.api.domain

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "game_jams",
    uniqueConstraints = [
        UniqueConstraint(name = "uq_game_jams_public_id", columnNames = ["public_id"])
    ],
    indexes = [
        Index(name = "idx_game_jams_organizer_id", columnList = "organizer_id"),
        Index(name = "idx_game_jams_status", columnList = "status"),
        Index(name = "idx_game_jams_dates", columnList = "start_date,end_date"),
        Index(name = "idx_game_jams_created_at", columnList = "created_at")
    ]
)
class GameJam(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "public_id", nullable = false, columnDefinition = "uuid")
    var publicId: UUID = UUID.randomUUID(),

    @Column(name = "organizer_id", nullable = false)
    var organizerId: Long,

    @Column(nullable = false, length = 200)
    var name: String,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(length = 200)
    var theme: String? = null,

    @Column(columnDefinition = "TEXT")
    var rules: String? = null,

    @Column(name = "start_date", nullable = false)
    var startDate: Instant,

    @Column(name = "end_date", nullable = false)
    var endDate: Instant,

    @Column(name = "submission_deadline", nullable = false)
    var submissionDeadline: Instant,

    @Column(name = "judging_end_date")
    var judgingEndDate: Instant? = null,

    @Column(name = "max_team_size", nullable = false)
    var maxTeamSize: Int = 10,

    @Column(name = "min_team_size", nullable = false)
    var minTeamSize: Int = 1,

    @Column(nullable = false, columnDefinition = "game_jam_status")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    var status: GameJamStatus = GameJamStatus.DRAFT,

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    var createdAt: Instant? = null,

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    var updatedAt: Instant? = null
)

enum class GameJamStatus {
    DRAFT,
    ANNOUNCED,
    IN_PROGRESS,
    JUDGING,
    COMPLETED,
    CANCELLED
}