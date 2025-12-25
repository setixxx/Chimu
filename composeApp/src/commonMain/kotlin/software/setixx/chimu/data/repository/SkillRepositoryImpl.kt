package software.setixx.chimu.data.repository

import software.setixx.chimu.data.remote.SkillApi
import software.setixx.chimu.domain.model.Skill
import software.setixx.chimu.domain.repository.SkillRepository

class SkillRepositoryImpl(
    private val api: SkillApi
) : SkillRepository {

    override suspend fun getAllSkills(): Result<List<Skill>> {
        return try {
            val response = api.getAllSkills()
            val skills = response.map { dto ->
                Skill(
                    id = dto.id,
                    name = dto.name
                )
            }
            Result.success(skills)
        } catch (e: Exception) {
            println("❌ Error loading skills: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
}