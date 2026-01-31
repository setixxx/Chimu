package software.setixx.chimu.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import software.setixx.chimu.data.remote.dto.AuthResponse
import software.setixx.chimu.data.remote.dto.LoginRequest
import software.setixx.chimu.data.remote.dto.RefreshTokenRequest
import software.setixx.chimu.data.remote.dto.RegisterRequest
import software.setixx.chimu.data.remote.dto.RegisterResponse
import software.setixx.chimu.data.remote.dto.TokenResponse
import software.setixx.chimu.data.remote.dto.UserProfileResponse

class AuthApi(private val client: HttpClient) {

    suspend fun login(email: String, password: String): AuthResponse {
        val response = client.post("/api/auth") {
            setBody(LoginRequest(email, password))
        }
        return response.body()
    }

    suspend fun register(email: String, password: String): RegisterResponse {
        val response = client.post("/api/auth/register") {
            setBody(RegisterRequest(email, password))
        }
        return response.body()
    }

    suspend fun refreshToken(refreshToken: String): TokenResponse {
        val response = client.post("/api/auth/refresh") {
            setBody(RefreshTokenRequest(refreshToken))
        }
        return response.body()
    }

    suspend fun logout(refreshToken: String) {
        val response = client.post("/api/auth/logout") {
            setBody(RefreshTokenRequest(refreshToken))
        }
        return response.body()
    }

    suspend fun getCurrentUser(accessToken: String): UserProfileResponse {
        val response = client.get("/api/users/me") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        return response.body()
    }
}