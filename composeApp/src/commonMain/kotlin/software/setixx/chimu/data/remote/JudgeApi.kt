package software.setixx.chimu.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import software.setixx.chimu.data.remote.dto.AssignJudgeRequest
import software.setixx.chimu.data.remote.dto.JudgeResponse

class JudgeApi(
    private val client: HttpClient
) {
    suspend fun getJamJudges(
        jamId: String,
        accessToken: String
    ): List<JudgeResponse>{
        val response = client.get("/api/jams/$jamId/judges"){
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            404 -> throw IllegalArgumentException("Игра не найдена")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }

    suspend fun assignJudge(
        jamId: String,
        accessToken: String,
        body: AssignJudgeRequest
    ): JudgeResponse {
        val response = client.post("/api/jams/$jamId/judges") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            setBody(body)
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            403 -> throw IllegalArgumentException("Недостаточно прав для назначения судьи")
            404 -> throw IllegalArgumentException("Игра или судья не найдены")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }

    suspend fun unassignJudge(
        jamId: String,
        judgeUserId: String,
        accessToken: String
    ){
        val response = client.delete("/api/jams/$jamId/judges/$judgeUserId"){
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            403 -> throw IllegalArgumentException("Недостаточно прав для снятия судьи")
            404 -> throw IllegalArgumentException("Игра или судья не найдены")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }
}