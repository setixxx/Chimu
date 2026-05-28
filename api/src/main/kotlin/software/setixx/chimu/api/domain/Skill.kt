package software.setixx.chimu.api.domain

import jakarta.persistence.*
import java.util.UUID

/**
 * Сущность навыка (технологии).
 * Используется для указания компетенций пользователей в их профилях.
 */
@Entity
@Table(name = "skills")
class Skill(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "public_id", nullable = false, columnDefinition = "uuid")
    var publicId: UUID = UUID.randomUUID(),

    @Column(nullable = false, unique = true, length = 50)
    var name: String
)