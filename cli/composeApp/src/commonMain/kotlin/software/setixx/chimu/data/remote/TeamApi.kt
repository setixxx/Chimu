package software.setixx.chimu.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.HttpHeaders
import software.setixx.chimu.data.remote.dto.*

class TeamApi(private val client: HttpClient) {

    suspend fun getUserTeams(accessToken: String): List<TeamResponse> {
        val response = client.get("/api/teams") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }

    suspend fun createTeam(accessToken: String, request: CreateTeamRequest): TeamDetailsResponse {
        val response = client.post("/api/teams") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            setBody(request)
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            400 -> throw IllegalArgumentException("Проверьте правильность введенных данных")
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }

    suspend fun getTeamDetails(accessToken: String, teamId: String): TeamDetailsResponse {
        val response = client.get("/api/teams/$teamId") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            404 -> throw IllegalArgumentException("Команда не найдена")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }

    suspend fun updateTeam(accessToken: String, teamId: String, request: UpdateTeamRequest): TeamDetailsResponse {
        val response = client.patch("/api/teams/$teamId") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            setBody(request)
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            400 -> throw IllegalArgumentException("Проверьте правильность введенных данных")
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            403 -> throw IllegalArgumentException("Недостаточно прав для обновления команды")
            404 -> throw IllegalArgumentException("Команда не найдена")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }

    suspend fun joinTeam(accessToken: String, inviteToken: String): TeamDetailsResponse {
        val response = client.post("/api/teams/$inviteToken") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            400 -> throw IllegalArgumentException("Неправильный токен или вы уже состоите в команде")
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            404 -> throw IllegalArgumentException("Команда не найдена")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }

    suspend fun leaveTeam(accessToken: String, teamId: String) {
        val response = client.delete("/api/teams/$teamId/leave") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            400 -> throw IllegalArgumentException("Нельзя покинуть команду пока есть активная регистрация на джем")
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            403 -> throw IllegalArgumentException("Лидер не может покинуть команду")
            404 -> throw IllegalArgumentException("Команда не найдена")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }

    suspend fun deleteTeam(accessToken: String, teamId: String) {
        val response = client.delete("/api/teams/$teamId") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            400 -> throw IllegalArgumentException("Нельзя удалить команду пока есть активная регистрация на джем")
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            403 -> throw IllegalArgumentException("Недостаточно прав для удаления команды")
            404 -> throw IllegalArgumentException("Команда не найдена")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }

    // TODO проверить может ли лидер кикнуть сам себя
    suspend fun kickMember(accessToken: String, teamId: String, userId: String) {
        val response = client.delete("/api/teams/$teamId/members/$userId") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            400 -> throw IllegalArgumentException("Нельзя удалить участника пока есть активная регистрация на джем")
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            403 -> throw IllegalArgumentException("Недостаточно прав для удаления участника")
            404 -> throw IllegalArgumentException("Команда или участник не найдены")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }

    suspend fun updateMemberSpecialization(
        accessToken: String,
        teamId: String,
        request: UpdateMemberSpecializationRequest
    ): TeamMemberResponse {
        val response = client.patch("/api/teams/$teamId/specialization") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            setBody(request)
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            400 -> throw IllegalArgumentException("Проверьте правильность введенных данных")
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            403 -> throw IllegalArgumentException("Недостаточно прав для обновления специализации")
            404 -> throw IllegalArgumentException("Команда или специализация не найдены")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }

    suspend fun regenerateInviteToken(accessToken: String, teamId: String): Map<String, String> {
        val response = client.post("/api/teams/$teamId/regenerate-token") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            403 -> throw IllegalArgumentException("Недостаточно прав для обновления токена")
            404 -> throw IllegalArgumentException("Команда не найдена")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }
}