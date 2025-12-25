package software.setixx.chimu.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import software.setixx.chimu.data.remote.dto.CreateTeamRequest
import software.setixx.chimu.data.remote.dto.TeamDetailsResponse
import software.setixx.chimu.data.remote.dto.TeamResponse

class TeamApi(private val client: HttpClient) {

    suspend fun getUserTeams(accessToken: String): List<TeamResponse> {
        return client.get("/api/teams") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }.body()
    }

    suspend fun createTeam(accessToken: String, request: CreateTeamRequest): TeamDetailsResponse {
        return client.post("/api/teams") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            setBody(request)
        }.body()
    }
}