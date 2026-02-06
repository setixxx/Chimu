package software.setixx.chimu.data.repository

import software.setixx.chimu.data.local.TokenStorage
import software.setixx.chimu.data.remote.AuthApi
import software.setixx.chimu.domain.model.*
import software.setixx.chimu.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val tokenStorage: TokenStorage
) : AuthRepository {

    override suspend fun login(
        email: String,
        password: String,
        rememberMe: Boolean
    ): ApiResult<AuthTokens> {
        return try {
            val response = authApi.login(email, password)

            tokenStorage.saveAccessToken(response.accessToken)
            tokenStorage.saveRefreshToken(response.refreshToken)

            if (rememberMe) {
                tokenStorage.saveEmail(email)
            }

            ApiResult.Success(
                AuthTokens(
                    accessToken = response.accessToken,
                    refreshToken = response.refreshToken
                )
            )
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        } catch (e: IllegalArgumentException) {
            ApiResult.Error(e.message ?: "Неизвестная ошибка")
        }
    }

    override suspend fun register(email: String, password: String): ApiResult<String> {
        return try {
            val response = authApi.register(email, password)
            ApiResult.Success(response.publicId)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        } catch (e: IllegalArgumentException) {
            ApiResult.Error(e.message ?: "Неизвестная ошибка")
        }
    }

    override suspend fun logout(): ApiResult<Unit> {
        return try {
            val refreshToken = tokenStorage.getRefreshToken()
            if (refreshToken != null) {
                authApi.logout(refreshToken)
            }
            tokenStorage.clearTokens()
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            tokenStorage.clearTokens()
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        } catch (e: IllegalArgumentException) {
            ApiResult.Error(e.message ?: "Неизвестная ошибка")
        }
    }

    override suspend fun refreshAccessToken(): ApiResult<String> {
        return try {
            val refreshToken = tokenStorage.getRefreshToken()
                ?: return ApiResult.Error("Refresh token не найден")

            val response = authApi.refreshToken(refreshToken)
            tokenStorage.saveAccessToken(response.token)

            ApiResult.Success(response.token)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Не удалось обновить токен")
        } catch (e: IllegalArgumentException) {
            ApiResult.Error(e.message ?: "Неизвестная ошибка")
        }
    }

    override suspend fun isLoggedIn(): Boolean {
        return tokenStorage.getAccessToken() != null &&
                tokenStorage.getRefreshToken() != null
    }

    override suspend fun getSavedEmail(): String? {
        return tokenStorage.getEmail()
    }
}