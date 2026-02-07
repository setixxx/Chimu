package software.setixx.chimu.data.repository

import software.setixx.chimu.data.local.TokenStorage
import software.setixx.chimu.data.remote.RatingCriteriaApi
import software.setixx.chimu.data.remote.dto.CreateRatingCriteriaRequest
import software.setixx.chimu.data.remote.dto.RatingCriteriaResponse
import software.setixx.chimu.data.remote.dto.UpdateRatingCriteriaRequest
import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.model.CreateRatingCriteria
import software.setixx.chimu.domain.model.RatingCriteria
import software.setixx.chimu.domain.model.UpdateRatingCriteria
import software.setixx.chimu.domain.repository.RatingCriteriaRepository

class RatingCriteriaRepositoryImpl(
    private val api: RatingCriteriaApi,
    private val tokenStorage: TokenStorage
) : RatingCriteriaRepository {

    override suspend fun getJamCriteria(jamId: String): ApiResult<List<RatingCriteria>> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")

            val response = api.getJamCriteria(jamId, token)
            ApiResult.Success(response.map { it.toDomain() })
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    override suspend fun createJamCriteria(
        jamId: String,
        data: CreateRatingCriteria
    ): ApiResult<RatingCriteria> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")

            val request = CreateRatingCriteriaRequest(
                name = data.name,
                description = data.description,
                maxScore = data.maxScore,
                weight = data.weight,
                orderIndex = data.orderIndex
            )

            val response = api.createJamCriteria(request, jamId, token)
            ApiResult.Success(response.toDomain())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    override suspend fun deleteJamCriteria(jamId: String, criteriaId: Long): ApiResult<Unit> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")

            api.deleteJamCriteria(jamId, criteriaId, token)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    override suspend fun updateJamCriteria(
        jamId: String,
        criteriaId: Long,
        data: UpdateRatingCriteria
    ): ApiResult<RatingCriteria> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")

            val request = UpdateRatingCriteriaRequest(
                name = data.name,
                description = data.description,
                maxScore = data.maxScore,
                weight = data.weight,
                orderIndex = data.orderIndex
            )

            val response = api.updateJamCriteria(request, jamId, criteriaId, token)
            ApiResult.Success(response.toDomain())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    private fun RatingCriteriaResponse.toDomain(): RatingCriteria {
        return RatingCriteria(
            id = id,
            name = name,
            description = description,
            maxScore = maxScore,
            weight = weight,
            orderIndex = orderIndex
        )
    }
}
