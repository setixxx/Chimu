package software.setixx.chimu.api.domain

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(
    name = "rating_criteria",
    indexes = [
        Index(name = "idx_rating_criteria_jam_id", columnList = "jam_id"),
        Index(name = "idx_rating_criteria_order", columnList = "jam_id,order_index")
    ]
)
class RatingCriteria(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "jam_id", nullable = false)
    var jamId: Long,

    @Column(nullable = false, length = 100)
    var name: String,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(name = "max_score", nullable = false)
    var maxScore: Int = 10,

    @Column(nullable = false, precision = 3, scale = 2)
    var weight: BigDecimal = BigDecimal.ONE,

    @Column(name = "order_index", nullable = false)
    var orderIndex: Int = 0,

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    var createdAt: Instant? = null
)