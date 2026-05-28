package software.setixx.chimu.api.domain

import jakarta.persistence.*
import org.hibernate.annotations.Generated
import org.hibernate.generator.EventType
import java.time.Instant

/**
 * Связующая сущность для назначения судей на Game Jam.
 * Хранит информацию о том, кто и когда назначил пользователя судьей мероприятия.
 */
@Entity
@Table(name = "jam_judges")
class JamJudge(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jam_id", nullable = false)
    val gameJam: GameJam,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "judge_id", nullable = false)
    val judge: User,

    @Generated(event = [EventType.INSERT])
    @Column(name = "assigned_at", nullable = false, insertable = false, updatable = false)
    var assignedAt: Instant? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by", nullable = false)
    val assignedBy: User,

    @Column(name = "deleted_at")
    var deletedAt: Instant? = null
)