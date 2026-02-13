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
import software.setixx.chimu.data.remote.dto.JudgeProgressResponse
import software.setixx.chimu.data.remote.dto.MyRatingResponse
import software.setixx.chimu.data.remote.dto.ProjectRatingResponse
import software.setixx.chimu.data.remote.dto.RateProjectRequest
import software.setixx.chimu.data.remote.dto.RatingResponse
import software.setixx.chimu.data.remote.dto.UpdateRatingRequest

class RatingApi(
    private val client: HttpClient
) {
    suspend fun getProjectRatings(
        projectId: String,
        accessToken: String
    ): ProjectRatingResponse {
        val response = client.get("/api/projects/$projectId/ratings") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            403 -> throw IllegalArgumentException("Недостаточно прав для получения рейтинга")
            404 -> throw IllegalArgumentException("Проект не найден")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }

    suspend fun rateProject(
        projectId: String,
        body: RateProjectRequest,
        accessToken: String
    ): RatingResponse {
        val response = client.post("/api/projects/$projectId/ratings") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            setBody(body)
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            400 -> throw IllegalArgumentException("Проверьте правильность введенных данных")
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            403 -> throw IllegalArgumentException("Вы не можете оценить проект")
            404 -> throw IllegalArgumentException("Проект не найден")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }
    suspend fun deleteProjectRating(
        ratingId: String,
        accessToken: String
    ) {
        val response = client.delete("/api/ratings/$ratingId"){
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            403 -> throw IllegalArgumentException("Не ваш рейтинг или окончена стадия оценивания")
            404 -> throw IllegalArgumentException("Рейтинг не найден")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }
    suspend fun updateProjectRating(
        ratingId: String,
        body: UpdateRatingRequest,
        accessToken: String
    ): RatingResponse {
        val response = client.patch("/api/ratings/$ratingId") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            setBody(body)
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            400 -> throw IllegalArgumentException("Проверьте правильность введенных данных")
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            403 -> throw IllegalArgumentException("Не ваш рейтинг или окончена стадия оценивания")
            404 -> throw IllegalArgumentException("Рейтинг не найден")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }
    suspend fun getMyRatings(
        projectId: String,
        accessToken: String
    ): List<MyRatingResponse> {
        val response = client.get("/api/projects/$projectId/my-ratings"){
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            403 -> throw IllegalArgumentException("Недостаточно прав для получения своих оценок")
            404 -> throw IllegalArgumentException("Проект не найден")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }

    // TODO поменять везде и на клиенте и на сервере с my на judge
    suspend fun getJudgeProgress(
        jamId: String,
        accessToken: String
    ): JudgeProgressResponse {
        val response = client.get("/api/jams/$jamId/my-progress"){
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            403 -> throw IllegalArgumentException("Недостаточно прав для получения информации о прохождении судьи")
            404 -> throw IllegalArgumentException("Игра не найдена")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }
}