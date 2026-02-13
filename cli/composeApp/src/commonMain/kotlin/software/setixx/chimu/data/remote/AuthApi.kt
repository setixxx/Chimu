package software.setixx.chimu.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import software.setixx.chimu.data.remote.dto.LoginResponse
import software.setixx.chimu.data.remote.dto.LoginRequest
import software.setixx.chimu.data.remote.dto.RefreshTokenRequest
import software.setixx.chimu.data.remote.dto.RegisterRequest
import software.setixx.chimu.data.remote.dto.RegisterResponse
import software.setixx.chimu.data.remote.dto.TokenResponse
import software.setixx.chimu.data.remote.dto.UserProfileResponse

class AuthApi(private val client: HttpClient) {

    suspend fun login(email: String, password: String): LoginResponse {
        val response = client.post("/api/auth") {
            setBody(LoginRequest(email, password))
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }

    suspend fun register(email: String, password: String): RegisterResponse {
        val response = client.post("/api/auth/register") {
            setBody(RegisterRequest(email, password))
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            400 -> throw IllegalArgumentException("Проверьте правильность введенных данных")
            409 -> throw IllegalArgumentException("Пользователь с таким email уже существует")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }

    suspend fun refreshToken(refreshToken: String): TokenResponse {
        val response = client.post("/api/auth/refresh") {
            setBody(RefreshTokenRequest(refreshToken))
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            401 -> throw IllegalArgumentException("Ошибка обновления токена")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }

    suspend fun logout(refreshToken: String) {
        val response = client.post("/api/auth/logout") {
            setBody(RefreshTokenRequest(refreshToken))
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            401 -> throw IllegalArgumentException("Ошибка выхода")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }

}