package software.setixx.chimu.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import software.setixx.chimu.data.remote.dto.TeamResponse

class TeamApi(private val client: HttpClient) {
    suspend fun getUserTeams(accessToken: String): List<TeamResponse> {
        return client.get("/api/teams") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }.body()
    }
}