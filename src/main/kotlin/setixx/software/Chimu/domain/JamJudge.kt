package setixx.software.Chimu.domain

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "jam_judges",
    uniqueConstraints = [
        UniqueConstraint(name = "uq_jam_judges_jam_judge", columnNames = ["jam_id", "judge_id"])
    ],
    indexes = [
        Index(name = "idx_jam_judges_jam_id", columnList = "jam_id"),
        Index(name = "idx_jam_judges_judge_id", columnList = "judge_id"),
        Index(name = "idx_jam_judges_assigned_by", columnList = "assigned_by")
    ]
)
class JamJudge(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "jam_id", nullable = false)
    var jamId: Long,

    @Column(name = "judge_id", nullable = false)
    var judgeId: Long,

    @Column(name = "assigned_at", nullable = false, insertable = false, updatable = false)
    var assignedAt: Instant? = null,

    @Column(name = "assigned_by", nullable = false)
    var assignedBy: Long
)