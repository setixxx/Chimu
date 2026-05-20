package software.setixx.chimu.api.repository

import org.springframework.data.jpa.repository.JpaRepository
import software.setixx.chimu.api.domain.Skill
import java.util.UUID

interface SkillRepository : JpaRepository<Skill, Long> {
    fun findByName(name: String): Skill?

    fun findAllByPublicIdIn(publicIds: List<UUID>): List<Skill>
}