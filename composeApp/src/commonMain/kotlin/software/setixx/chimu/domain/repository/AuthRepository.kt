package software.setixx.chimu.domain.repository

import software.setixx.chimu.domain.model.AuthResult
import software.setixx.chimu.domain.model.AuthTokens
import software.setixx.chimu.domain.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String, rememberMe: Boolean): AuthResult<AuthTokens>
    suspend fun register(email: String, password: String): AuthResult<String>
    suspend fun logout(): AuthResult<Unit>
    suspend fun getCurrentUser(): AuthResult<User>
    suspend fun refreshAccessToken(): AuthResult<String>
    suspend fun isLoggedIn(): Boolean
    suspend fun getSavedEmail(): String?
}