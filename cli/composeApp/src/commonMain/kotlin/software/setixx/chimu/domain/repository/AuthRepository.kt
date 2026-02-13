package software.setixx.chimu.domain.repository

import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.model.AuthTokens

interface AuthRepository {
    suspend fun login(email: String, password: String, rememberMe: Boolean): ApiResult<AuthTokens>
    suspend fun register(email: String, password: String): ApiResult<String>
    suspend fun logout(): ApiResult<Unit>
    suspend fun refreshAccessToken(): ApiResult<String>
    suspend fun isLoggedIn(): Boolean
    suspend fun getSavedEmail(): String?
}