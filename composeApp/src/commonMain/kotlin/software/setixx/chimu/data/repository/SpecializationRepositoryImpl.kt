package software.setixx.chimu.data.repository

import software.setixx.chimu.data.local.TokenStorage
import software.setixx.chimu.data.remote.SpecializationApi
import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.model.Specialization
import software.setixx.chimu.domain.repository.SpecializationRepository

class SpecializationRepositoryImpl(
    private val api: SpecializationApi,
    private val tokenStorage: TokenStorage
) : SpecializationRepository {

    override suspend fun getAllSpecializations(): ApiResult<List<Specialization>> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")

            val response = api.getAllSpecializations(token)
            val specializations = response.map { dto ->
                Specialization(
                    id = dto.id,
                    name = dto.name,
                    description = dto.description
                )
            }
            ApiResult.Success(specializations)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }
}