package software.setixx.chimu.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import software.setixx.chimu.api.domain.ProjectStatus
import software.setixx.chimu.data.remote.dto.CreateProjectRequest
import software.setixx.chimu.data.remote.dto.ProjectDetailsResponse
import software.setixx.chimu.data.remote.dto.ProjectResponse
import software.setixx.chimu.data.remote.dto.UpdateProjectRequest

class ProjectApi(private val client: HttpClient) {
    suspend fun submitProject(
        accessToken: String,
        projectId: String
    ): ProjectDetailsResponse {
        val response = client.get("/api/projects/$projectId/submit") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            404 -> throw IllegalArgumentException("Проект не найден")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }

    suspend fun returnDraft(
        accessToken: String,
        projectId: String
    ): ProjectDetailsResponse {
        val response = client.get("/api/projects/$projectId/return-draft") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            404 -> throw IllegalArgumentException("Проект не найден")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }

    suspend fun publishProject(
        accessToken: String,
        projectId: String
    ): ProjectDetailsResponse {
        val response = client.get("/api/projects/$projectId/publish") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            404 -> throw IllegalArgumentException("Проект не найден")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }

    suspend fun disqualifyProject(
        accessToken: String,
        projectId: String
    ): ProjectDetailsResponse {
        val response = client.get("/api/projects/$projectId/disqualify") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            404 -> throw IllegalArgumentException("Проект не найден")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }
    suspend fun getJamProjects(
        accessToken: String,
        jamId: String,
        status: ProjectStatus
    ): List<ProjectResponse>{
        val response = client.get("/api/jams/$jamId/projects?status=$status") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            404 -> throw IllegalArgumentException("Игра не найдена")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }
    suspend fun createProject(
        accessToken: String,
        jamId: String,
        body: CreateProjectRequest
    ): ProjectDetailsResponse{
        val response = client.post("/api/jams/$jamId/projects") {
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
    suspend fun getProject(
        accessToken: String,
        projectId: String
    ): ProjectDetailsResponse{
        val response = client.get("/api/projects/$projectId") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            404 -> throw IllegalArgumentException("Проект не найден")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }
    suspend fun deleteProject(
        accessToken: String,
        projectId: String
    ){
        val response = client.delete("/api/projects/$projectId") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            404 -> throw IllegalArgumentException("Проект не найден")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }
    suspend fun updateProject(
        accessToken: String,
        projectId: String,
        body: UpdateProjectRequest
    ): ProjectDetailsResponse {
        val response = client.post("/api/projects/$projectId") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            setBody(body)
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            404 -> throw IllegalArgumentException("Проект не найден")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }

    suspend fun getUserProjects(accessToken: String): List<ProjectResponse> {
        val response = client.get("/api/users/me/projects") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }

    suspend fun getTeamProjects(
        accessToken: String,
        teamId: String
    ): List<ProjectResponse>{
        val response = client.get("/api/teams/$teamId/projects") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            404 -> throw IllegalArgumentException("Команда не найдена")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }
}