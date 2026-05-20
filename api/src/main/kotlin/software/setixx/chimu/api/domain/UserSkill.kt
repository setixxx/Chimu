package software.setixx.chimu.api.domain

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "user_skills")
class UserSkill(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    val skill: Skill
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UserSkill) return false

        if (id != null && other.id != null) {
            return id == other.id
        }

        return user.publicId == other.user.publicId && skill.publicId == other.skill.publicId
    }

    override fun hashCode(): Int {
        return 31 * user.publicId.hashCode() + skill.publicId.hashCode()
    }
}