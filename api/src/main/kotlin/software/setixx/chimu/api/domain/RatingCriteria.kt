package software.setixx.chimu.api.domain

import jakarta.persistence.*
import org.hibernate.annotations.Generated
import org.hibernate.generator.EventType
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "rating_criteria")
class RatingCriteria(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jam_id", nullable = false)
    val gameJam: GameJam,

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

    @Generated(event = [EventType.INSERT])
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    var createdAt: Instant? = null,

    @Column(name = "deleted_at")
    var deletedAt: Instant? = null
)