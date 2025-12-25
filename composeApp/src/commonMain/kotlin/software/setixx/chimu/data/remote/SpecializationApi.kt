package software.setixx.chimu.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import software.setixx.chimu.data.remote.dto.SpecializationResponse

class SpecializationApi(private val client: HttpClient) {

    suspend fun getAllSpecializations(): List<SpecializationResponse> {
        return client.get("/api/specializations").body()
    }
}
