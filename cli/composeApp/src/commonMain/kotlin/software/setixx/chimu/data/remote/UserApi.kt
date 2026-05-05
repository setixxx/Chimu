package software.setixx.chimu.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import software.setixx.chimu.data.remote.dto.ChangePasswordRequest
import software.setixx.chimu.data.remote.dto.ChangePasswordResponse
import software.setixx.chimu.data.remote.dto.UpdateProfileRequest
import software.setixx.chimu.data.remote.dto.UserProfileResponse

class UserApi(private val client: HttpClient) {
    suspend fun changePassword(
        accessToken: String,
        body: ChangePasswordRequest
    ): ChangePasswordResponse {
        val response = client.patch("/api/users/password") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            setBody(body)
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

    suspend fun deleteProfile(accessToken: String) {
        val response = client.delete("/api/users/me") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        when (response.status.value) {
            in 200..299 -> return
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }

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

    suspend fun getUserById(accessToken: String, userId: String): UserProfileResponse {
        val response = client.get("/api/users/$userId") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            404 -> throw IllegalArgumentException("Пользователь не найден")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }
}