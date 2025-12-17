package software.setixx.chimu.data.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import software.setixx.chimu.data.remote.dto.*

class AuthApi(private val client: HttpClient) {

    suspend fun login(email: String, password: String): AuthResponse {
        return client.post("/api/auth") {
            setBody(LoginRequest(email, password))
        }.body()
    }

    suspend fun register(email: String, password: String): RegisterResponse {
        return client.post("/api/auth/register") {
            setBody(RegisterRequest(email, password))
        }.body()
    }

    suspend fun refreshToken(refreshToken: String): TokenResponse {
        return client.post("/api/auth/refresh") {
            setBody(RefreshTokenRequest(refreshToken))
        }.body()
    }

    suspend fun logout(refreshToken: String) {
        client.post("/api/auth/logout") {
            setBody(RefreshTokenRequest(refreshToken))
        }
    }

    suspend fun getCurrentUser(accessToken: String): UserProfileResponse {
        return client.get("/api/users/me") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }.body()
    }
}