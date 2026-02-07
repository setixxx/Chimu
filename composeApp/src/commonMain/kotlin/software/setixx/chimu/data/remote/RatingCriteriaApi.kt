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
import software.setixx.chimu.data.remote.dto.CreateRatingCriteriaRequest
import software.setixx.chimu.data.remote.dto.RatingCriteriaResponse
import software.setixx.chimu.data.remote.dto.UpdateRatingCriteriaRequest

class RatingCriteriaApi(
    private val client: HttpClient
) {
    suspend fun getJamCriteria(
        jamId: String,
        accessToken: String
    ): List<RatingCriteriaResponse> {
        val response = client.get("/api/jams/$jamId/criteria"){
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            404 -> throw IllegalArgumentException("Игра не найдена")
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }

    suspend fun createJamCriteria(
        body: CreateRatingCriteriaRequest,
        jamId: String,
        accessToken: String
    ): RatingCriteriaResponse {
        val response = client.post("/api/jams/$jamId/criteria"){
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            setBody(body)
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            404 -> throw IllegalArgumentException("Игра не найдена")
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            403 -> throw IllegalArgumentException("Недостаточно прав для создания критерия")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }

    suspend fun deleteJamCriteria(
        jamId: String,
        criteriaId: Long,
        accessToken: String
    ){
        val response = client.delete("/api/jams/$jamId/criteria/$criteriaId"){
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            404 -> throw IllegalArgumentException("Игра или критерий не найдены")
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            403 -> throw IllegalArgumentException("Недостаточно прав для удаления критерия")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }

    suspend fun updateJamCriteria(
        body: UpdateRatingCriteriaRequest,
        jamId: String,
        criteriaId: Long,
        accessToken: String
    ): RatingCriteriaResponse {
        val response = client.patch("/api/jams/$jamId/criteria/$criteriaId"){
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            setBody(body)
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            403 -> throw IllegalArgumentException("Недостаточно прав для обновления критерия")
            404 -> throw IllegalArgumentException("Игра или критерий не найдены")
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }
}