package software.setixx.chimu.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import software.setixx.chimu.data.remote.dto.SpecializationResponse

class SpecializationApi(private val client: HttpClient) {

    suspend fun getAllSpecializations(accessToken: String): List<SpecializationResponse> {
        return client.get("/api/specializations"){
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }.body()
    }
}
