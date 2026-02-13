package software.setixx.chimu.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import software.setixx.chimu.data.remote.dto.ProjectResponse

class ProjectApi(private val client: HttpClient) {
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

    suspend fun submitProject(){}
    suspend fun returnDraft(){}
    suspend fun publishProject(){}
    suspend fun getProjectFiles(){}
    suspend fun uploadProjectFiles(){}
    suspend fun disqualifyProject(){}
    suspend fun getJamProjects(){}
    suspend fun createProject(){}
    suspend fun getProject(){}
    suspend fun deleteProject(){}
    suspend fun updateProject(){}
    suspend fun getTeamProjects(){}
}