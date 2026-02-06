package software.setixx.chimu.data.repository

import software.setixx.chimu.data.local.TokenStorage
import software.setixx.chimu.data.remote.SkillApi
import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.model.Skill
import software.setixx.chimu.domain.repository.SkillRepository

class SkillRepositoryImpl(
    private val api: SkillApi,
    private val tokenStorage: TokenStorage
) : SkillRepository {

    override suspend fun getAllSkills(): ApiResult<List<Skill>> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")

            val response = api.getAllSkills(token)
            val skills = response.map { dto ->
                Skill(
                    id = dto.id,
                    name = dto.name
                )
            }
            ApiResult.Success(skills)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }
}