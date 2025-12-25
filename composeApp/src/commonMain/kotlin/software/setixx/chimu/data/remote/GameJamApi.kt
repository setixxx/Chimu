package software.setixx.chimu.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import software.setixx.chimu.data.remote.dto.GameJamResponse

class GameJamApi(private val client: HttpClient) {

    suspend fun getAllJams(accessToken: String): List<GameJamResponse> {
        return client.get("/api/jams") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }.body()
    }

    suspend fun getJamsByStatus(status: String, accessToken: String): List<GameJamResponse> {
        return client.get("/api/jams?status=$status") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }.body()
    }
}