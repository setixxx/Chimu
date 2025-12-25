package software.setixx.chimu.data.repository

import software.setixx.chimu.data.local.TokenStorage
import software.setixx.chimu.data.remote.SkillApi
import software.setixx.chimu.domain.model.Skill
import software.setixx.chimu.domain.repository.SkillRepository

class SkillRepositoryImpl(
    private val api: SkillApi,
    private val tokenStorage: TokenStorage
) : SkillRepository {

    override suspend fun getAllSkills(): Result<List<Skill>> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return Result.failure(Exception("Not authenticated"))

            val response = api.getAllSkills(token)
            val skills = response.map { dto ->
                Skill(
                    id = dto.id,
                    name = dto.name
                )
            }
            Result.success(skills)
        } catch (e: Exception) {
            println("Error loading skills: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
}