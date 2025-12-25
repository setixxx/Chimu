package software.setixx.chimu.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import software.setixx.chimu.data.remote.dto.ProjectResponse

class ProjectApi(private val client: HttpClient) {

    suspend fun getUserProjects(accessToken: String): List<ProjectResponse> {
        return client.get("/api/users/me/projects") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }.body()
    }
}