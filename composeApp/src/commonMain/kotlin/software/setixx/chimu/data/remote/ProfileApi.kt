package software.setixx.chimu.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import software.setixx.chimu.data.remote.dto.UpdateProfileRequest
import software.setixx.chimu.data.remote.dto.UserProfileResponse

class ProfileApi(private val client: HttpClient) {

    suspend fun updateProfile(accessToken: String, request: UpdateProfileRequest): UserProfileResponse {
        return client.patch("/api/users/me") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            setBody(request)
        }.body()
    }
}