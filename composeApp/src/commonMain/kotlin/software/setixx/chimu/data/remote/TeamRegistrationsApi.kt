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
import software.setixx.chimu.data.remote.dto.RegisterTeamRequest
import software.setixx.chimu.data.remote.dto.RegistrationResponse
import software.setixx.chimu.data.remote.dto.UpdateRegistrationStatusRequest
import software.setixx.chimu.domain.model.Registration

class TeamRegistrationsApi(
    private val client: HttpClient
) {
    suspend fun getJamRegistrations(
        jamId: String,
        accessToken: String
    ): List<RegistrationResponse>{
        val response = client.get("/api/jams/$jamId/registrations"){
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            404 -> throw IllegalArgumentException("Игра не найдена")
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }

    suspend fun registerTeam(
        jamId: String,
        accessToken: String,
        body: RegisterTeamRequest
    ): RegistrationResponse {
        val response = client.post("/api/jams/$jamId/registrations"){
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            setBody(body)
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            404 -> throw IllegalArgumentException("Команда не найдена")
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            403 -> throw IllegalArgumentException("Недостаточно прав для регистрации")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }

    }
    suspend fun withdrawTeam(
        jamId: String,
        teamId: String,
        accessToken: String
    ){
        val response = client.delete("/api/jams/$jamId/registrations/$teamId"){
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            404 -> throw IllegalArgumentException("Игра или команда не найдены")
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            403 -> throw IllegalArgumentException("Недостаточно прав для снятия регистрации")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }
    suspend fun updateRegistrationStatus(
        jamId: String,
        teamId: String,
        body: UpdateRegistrationStatusRequest,
        accessToken: String
    ): RegistrationResponse {
        val response = client.patch("/api/jams/$jamId/registrations/$teamId"){
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            setBody(body)
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            400 -> throw IllegalArgumentException("Проверьте правильность введенных данных")
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            403 -> throw IllegalArgumentException("Недостаточно прав для обновления статуса")
            404 -> throw IllegalArgumentException("Регистрация не найдена")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }
}