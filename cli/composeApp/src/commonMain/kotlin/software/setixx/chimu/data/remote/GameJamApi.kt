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
import software.setixx.chimu.data.remote.dto.CreateGameJamRequest
import software.setixx.chimu.data.remote.dto.GameJamDetailsResponse
import software.setixx.chimu.data.remote.dto.GameJamResponse
import software.setixx.chimu.data.remote.dto.UpdateGameJamRequest

class GameJamApi(private val client: HttpClient) {

    suspend fun getAllJams(accessToken: String): List<GameJamResponse> {
        val response = client.get("/api/jams") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }

    suspend fun getJamsByStatus(status: String, accessToken: String): List<GameJamResponse> {
        val response = client.get("/api/jams?status=$status") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }

    suspend fun createJam(body: CreateGameJamRequest, accessToken: String): GameJamDetailsResponse {
        val response = client.post("/api/jams") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            setBody(body)
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            403 -> throw IllegalArgumentException("Недостаточно прав для создания игры")
            400 -> throw IllegalArgumentException("Проверьте правильность введенных данных")
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }

    suspend fun getJamDetails(jamId: String, accessToken: String): GameJamDetailsResponse {
        val response = client.get("/api/jams/$jamId") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            404 -> throw IllegalArgumentException("Игра не найдена")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }

    suspend fun deleteJam(jamId: String, accessToken: String) {
        val response = client.delete("/api/jams/$jamId") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            403 -> throw IllegalArgumentException("Недостаточно прав для удаления игры")
            404 -> throw IllegalArgumentException("Игра не найдена")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }

    suspend fun updateJam(jamId: String, body: UpdateGameJamRequest, accessToken: String): GameJamDetailsResponse {
        val response = client.patch("/api/jams/$jamId") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            setBody(body)
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            403 -> throw IllegalArgumentException("Недостаточно прав для обновления игры")
            404 -> throw IllegalArgumentException("Игра не найдена")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }
}