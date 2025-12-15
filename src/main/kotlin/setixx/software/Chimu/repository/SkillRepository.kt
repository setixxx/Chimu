package setixx.software.Chimu.repository

import org.springframework.data.jpa.repository.JpaRepository
import setixx.software.Chimu.domain.Skill

interface SkillRepository : JpaRepository<Skill, Long> {
    fun findByName(name: String): Skill?
}