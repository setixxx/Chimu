package software.setixx.chimu.api.domain

import jakarta.persistence.*
import org.hibernate.annotations.Generated
import org.hibernate.generator.EventType
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "specializations")
class Specialization(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "public_id", nullable = false, columnDefinition = "uuid")
    var publicId: UUID = UUID.randomUUID(),

    @Column(nullable = false, unique = true, length = 100)
    var name: String,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Generated(event = [EventType.INSERT])
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    var createdAt: Instant? = null
)