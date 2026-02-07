package software.setixx.chimu.data.repository

import software.setixx.chimu.data.local.TokenStorage
import software.setixx.chimu.data.remote.JudgeApi
import software.setixx.chimu.data.remote.dto.AssignJudgeRequest
import software.setixx.chimu.data.remote.dto.JudgeResponse
import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.model.AssignJudge
import software.setixx.chimu.domain.model.Judge
import software.setixx.chimu.domain.repository.JudgeRepository

class JudgeRepositoryImpl(
    private val api: JudgeApi,
    private val tokenStorage: TokenStorage
) : JudgeRepository {

    override suspend fun getJamJudges(jamId: String): ApiResult<List<Judge>> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")

            val response = api.getJamJudges(jamId, token)
            ApiResult.Success(response.map { it.toDomain() })
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    override suspend fun assignJudge(jamId: String, data: AssignJudge): ApiResult<Judge> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")

            val request = AssignJudgeRequest(judgeUserId = data.judgeUserId)
            val response = api.assignJudge(jamId, token, request)
            ApiResult.Success(response.toDomain())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    override suspend fun unassignJudge(jamId: String, judgeUserId: String): ApiResult<Unit> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")

            api.unassignJudge(jamId, judgeUserId, token)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    private fun JudgeResponse.toDomain(): Judge {
        return Judge(
            userId = userId,
            nickname = nickname,
            avatarUrl = avatarUrl,
            assignedAt = assignedAt
        )
    }
}
