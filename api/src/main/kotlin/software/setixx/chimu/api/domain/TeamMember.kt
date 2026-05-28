package software.setixx.chimu.api.domain

import jakarta.persistence.*
import org.hibernate.annotations.Generated
import org.hibernate.generator.EventType
import java.time.Instant

/**
 * Сущность участника команды.
 * Связывает пользователя с командой и указывает его специализацию в её рамках.
 */
@Entity
@Table(name = "team_members")
class TeamMember(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    val team: Team,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialization_id")
    var specialization: Specialization? = null,

    @Generated(event = [EventType.INSERT])
    @Column(name = "joined_at", nullable = false, insertable = false, updatable = false)
    var joinedAt: Instant? = null,

    @Column(name = "deleted_at")
    var deletedAt: Instant? = null
)