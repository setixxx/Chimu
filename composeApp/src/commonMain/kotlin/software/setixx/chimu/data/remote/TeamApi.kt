package software.setixx.chimu.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.HttpHeaders
import software.setixx.chimu.data.remote.dto.*

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

    suspend fun getTeamDetails(accessToken: String, teamId: String): TeamDetailsResponse {
        return client.get("/api/teams/$teamId") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }.body()
    }

    suspend fun updateTeam(accessToken: String, teamId: String, request: UpdateTeamRequest): TeamDetailsResponse {
        return client.patch("/api/teams/$teamId") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            setBody(request)
        }.body()
    }

    suspend fun joinTeam(accessToken: String, inviteToken: String): TeamDetailsResponse {
        return client.post("/api/teams/$inviteToken") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }.body()
    }

    suspend fun leaveTeam(accessToken: String, teamId: String) {
        client.delete("/api/teams/$teamId/leave") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
    }

    suspend fun deleteTeam(accessToken: String, teamId: String) {
        client.delete("/api/teams/$teamId") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
    }

    suspend fun kickMember(accessToken: String, teamId: String, userId: String) {
        client.delete("/api/teams/$teamId/members/$userId") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
    }

    suspend fun updateMemberSpecialization(
        accessToken: String,
        teamId: String,
        request: UpdateMemberSpecializationRequest
    ): TeamMemberResponse {
        return client.patch("/api/teams/$teamId/specialization") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            setBody(request)
        }.body()
    }

    suspend fun regenerateInviteToken(accessToken: String, teamId: String): Map<String, String> {
        return client.post("/api/teams/$teamId/regenerate-token") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }.body()
    }
}