package software.setixx.chimu.domain.repository

import software.setixx.chimu.domain.model.Skill

interface SkillRepository {
    suspend fun getAllSkills(): Result<List<Skill>>
}