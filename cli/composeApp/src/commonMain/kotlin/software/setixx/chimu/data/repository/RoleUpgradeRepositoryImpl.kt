package software.setixx.chimu.data.repository

import software.setixx.chimu.data.local.TokenStorage
import software.setixx.chimu.data.remote.RoleUpgradeApi
import software.setixx.chimu.data.remote.dto.CreateRoleUpgradeRequest
import software.setixx.chimu.data.remote.dto.ReviewRoleUpgradeRequest
import software.setixx.chimu.data.remote.dto.RoleUpgradeResponse
import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.model.CreateRoleUpgrade
import software.setixx.chimu.domain.model.ReviewRoleUpgrade
import software.setixx.chimu.domain.model.RoleUpgrade
import software.setixx.chimu.domain.repository.RoleUpgradeRepository

class RoleUpgradeRepositoryImpl(
    private val api: RoleUpgradeApi,
    private val tokenStorage: TokenStorage
): RoleUpgradeRepository {
    override suspend fun getUserRoleUpgrades(): ApiResult<List<RoleUpgrade>> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: throw IllegalArgumentException("Ошибка аутентификации")
            val response = api.getUserRoleUpgrades(token)
            ApiResult.Success(response.map { it.toDomain() })
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    override suspend fun createRoleUpgrade(data: CreateRoleUpgrade): ApiResult<RoleUpgrade> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: throw IllegalArgumentException("Ошибка аутентификации")
            val request = CreateRoleUpgradeRequest(
                requestedRole = data.requestedRole,
                userMessage = data.userMessage
            )
            val response = api.createRoleUpgrade(request, token)
            ApiResult.Success(response.toDomain())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    override suspend fun reviewRoleUpgrade(
        requestId: String,
        data: ReviewRoleUpgrade
    ): ApiResult<RoleUpgrade> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: throw IllegalArgumentException("Ошибка аутентификации")
            val request = ReviewRoleUpgradeRequest(
                status = data.status,
                adminMessage = data.adminMessage
            )
            val response = api.reviewRoleUpgrade(requestId, request, token)
            ApiResult.Success(response.toDomain())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    override suspend fun getAllRoleUpgrades(): ApiResult<List<RoleUpgrade>> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: throw IllegalArgumentException("Ошибка аутентификации")
            val response = api.getAllRoleUpgrades(token)
            ApiResult.Success(response.map { it.toDomain() })
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    override suspend fun cancelRoleUpgrade(requestId: String): ApiResult<RoleUpgrade> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: throw IllegalArgumentException("Ошибка аутентификации")
            val response = api.cancelRoleUpgrade(requestId, token)
            ApiResult.Success(response.toDomain())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    private fun RoleUpgradeResponse.toDomain(): RoleUpgrade {
        return RoleUpgrade(
            id = id,
            userId = userId,
            userNickname = userNickname,
            requestedRole = requestedRole,
            status = status,
            userMessage = userMessage,
            adminMessage = adminMessage,
            reviewedBy = reviewedBy,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}