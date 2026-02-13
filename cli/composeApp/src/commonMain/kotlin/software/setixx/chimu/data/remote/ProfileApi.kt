package software.setixx.chimu.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import software.setixx.chimu.data.remote.dto.UpdateProfileRequest
import software.setixx.chimu.data.remote.dto.UserProfileResponse

class ProfileApi(private val client: HttpClient) {

    suspend fun updateProfile(accessToken: String, request: UpdateProfileRequest): UserProfileResponse {
        val response = client.patch("/api/users/me") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            setBody(request)
        }

        when (response.status.value) {
            in 200..299 -> return response.body()
            400 -> throw IllegalArgumentException("Проверьте правильность введенных данных")
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }

    suspend fun getCurrentUser(accessToken: String): UserProfileResponse {
        val response = client.get("/api/users/me") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }
}