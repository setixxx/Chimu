package software.setixx.chimu.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import software.setixx.chimu.data.remote.dto.CreateJamTransferRequest
import software.setixx.chimu.data.remote.dto.JamTransferResponse
import software.setixx.chimu.data.remote.dto.ReviewJamTransferRequest

class JamTransferApi(
    private val client: HttpClient
) {
    suspend fun createTransfer(
        jamId: String,
        body: CreateJamTransferRequest,
        accessToken: String
    ): JamTransferResponse {
        val response = client.post("/api/jams/$jamId/transfer") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            setBody(body)
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            404 -> throw IllegalArgumentException("Игра не найдена")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }

    suspend fun cancelTransfer(
        jamId: String,
        accessToken: String
    ): JamTransferResponse {
        val response = client.delete("/api/jams/$jamId/transfer") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            404 -> throw IllegalArgumentException("Игра не найдена")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }

    suspend fun reviewTransfer(
        requestId: String,
        body: ReviewJamTransferRequest,
        accessToken: String
    ): JamTransferResponse {
        val response = client.patch("/api/transfer-requests/$requestId") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            setBody(body)
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            404 -> throw IllegalArgumentException("Запрос не найден")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }

    suspend fun getUserTransfers(
        accessToken: String
    ): List<JamTransferResponse>{
        val response = client.get("/api/users/me/transfer-requests") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }
}