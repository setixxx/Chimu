package software.setixx.chimu.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import software.setixx.chimu.data.remote.dto.JamStatisticsResponse
import software.setixx.chimu.data.remote.dto.LeaderboardResponse

class LeaderboardApi(
    private val client: HttpClient
) {
    suspend fun getJamStatistics(
        jamId: String,
        accessToken: String
    ): JamStatisticsResponse {
        val response = client.get("/api/jams/$jamId/statistics"){
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            403 -> throw IllegalArgumentException("Недостаточно прав для получения статистики игры")
            404 -> throw IllegalArgumentException("Игра не найдена")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }

    suspend fun getLeaderboard(
        jamId: String,
        accessToken: String
    ): LeaderboardResponse {
        val response = client.get("/api/jams/$jamId/leaderboard"){
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        when (response.status.value){
            in 200..299 -> return response.body()
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            403 -> throw IllegalArgumentException("Недостаточно прав для получения лидеров игры")
            404 -> throw IllegalArgumentException("Игра не найдена")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }
}