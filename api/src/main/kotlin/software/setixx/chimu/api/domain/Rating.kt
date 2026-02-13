package software.setixx.chimu.api.domain

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(
    name = "ratings",
    uniqueConstraints = [
        UniqueConstraint(name = "uq_ratings_project_judge_criteria", columnNames = ["project_id", "judge_id", "criteria_id"])
    ],
    indexes = [
        Index(name = "idx_ratings_project_id", columnList = "project_id"),
        Index(name = "idx_ratings_judge_id", columnList = "judge_id"),
        Index(name = "idx_ratings_criteria_id", columnList = "criteria_id"),
        Index(name = "idx_ratings_created_at", columnList = "created_at")
    ]
)
class Rating(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "project_id", nullable = false)
    var projectId: Long,

    @Column(name = "judge_id", nullable = false)
    var judgeId: Long,

    @Column(name = "criteria_id", nullable = false)
    var criteriaId: Long,

    @Column(nullable = false, precision = 4, scale = 2)
    var score: BigDecimal,

    @Column(columnDefinition = "TEXT")
    var comment: String? = null,

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    var createdAt: Instant? = null,

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    var updatedAt: Instant? = null
)