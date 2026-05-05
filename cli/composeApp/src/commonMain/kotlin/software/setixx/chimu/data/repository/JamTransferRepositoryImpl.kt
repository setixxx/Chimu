package software.setixx.chimu.data.repository

import software.setixx.chimu.data.local.TokenStorage
import software.setixx.chimu.data.remote.JamTransferApi
import software.setixx.chimu.data.remote.dto.CreateJamTransferRequest
import software.setixx.chimu.data.remote.dto.JamTransferResponse
import software.setixx.chimu.data.remote.dto.ReviewJamTransferRequest
import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.model.CreateJamTransfer
import software.setixx.chimu.domain.model.JamTransfer
import software.setixx.chimu.domain.model.ReviewJamTransfer
import software.setixx.chimu.domain.repository.JamTransferRepository

class JamTransferRepositoryImpl(
    private val api: JamTransferApi,
    private val tokenStorage: TokenStorage
): JamTransferRepository {
    override suspend fun createTransferRequest(
        jamId: String,
        data: CreateJamTransfer
    ): ApiResult<JamTransfer> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")
            val request = CreateJamTransferRequest(
                recipientUserId = data.recipientUserId
            )
            val response = api.createTransfer(jamId, request, token)
            ApiResult.Success(response.toDomain())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    override suspend fun cancelTransferRequest(jamId: String): ApiResult<JamTransfer> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")
            val response = api.cancelTransfer(jamId, token)
            ApiResult.Success(response.toDomain())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    override suspend fun reviewTransferRequest(
        requestId: String,
        data: ReviewJamTransfer
    ): ApiResult<JamTransfer> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")
            val request = ReviewJamTransferRequest(
                status = data.status
            )
            val response = api.reviewTransfer(requestId, request, token)
            ApiResult.Success(response.toDomain())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    override suspend fun getTransferRequests(): ApiResult<List<JamTransfer>> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")
            val response = api.getUserTransfers(token)
            ApiResult.Success(response.map { it.toDomain() })
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    private fun JamTransferResponse.toDomain(): JamTransfer {
        return JamTransfer(
            id = id,
            jamId = jamId,
            jamName = jamName,
            senderId = senderId,
            senderNickname = senderNickname,
            recipientId = recipientId,
            recipientNickname = recipientNickname,
            status = status,
            expiresAt = expiresAt,
            createdAt = createdAt
        )
    }
}