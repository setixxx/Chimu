package software.setixx.chimu.data.repository

import io.ktor.client.plugins.*
import software.setixx.chimu.data.local.TokenStorage
import software.setixx.chimu.data.remote.AuthApi
import software.setixx.chimu.data.remote.dto.ErrorResponse
import software.setixx.chimu.domain.model.*
import software.setixx.chimu.domain.repository.AuthRepository
import io.ktor.client.call.*

class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val tokenStorage: TokenStorage
) : AuthRepository {

    override suspend fun login(
        email: String,
        password: String,
        rememberMe: Boolean
    ): AuthResult<AuthTokens> {
        return try {
            val response = authApi.login(email, password)

            tokenStorage.saveAccessToken(response.accessToken)
            tokenStorage.saveRefreshToken(response.refreshToken)

            if (rememberMe) {
                tokenStorage.saveEmail(email)
            }

            AuthResult.Success(
                AuthTokens(
                    accessToken = response.accessToken,
                    refreshToken = response.refreshToken
                )
            )
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Неизвестная ошибка")
        }
    }

    override suspend fun register(email: String, password: String): AuthResult<String> {
        return try {
            val response = authApi.register(email, password)
            AuthResult.Success(response.publicId)
        } catch (e: ClientRequestException) {
            val errorMessage = try {
                val errorResponse = e.response.body<ErrorResponse>()
                errorResponse.message
            } catch (_: Exception) {
                when (e.response.status.value) {
                    409 -> "Пользователь с таким email уже существует"
                    400 -> "Проверьте правильность введенных данных"
                    else -> "Ошибка регистрации"
                }
            }
            AuthResult.Error(errorMessage)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    override suspend fun logout(): AuthResult<Unit> {
        return try {
            val refreshToken = tokenStorage.getRefreshToken()
            if (refreshToken != null) {
                authApi.logout(refreshToken)
            }
            tokenStorage.clearTokens()
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            tokenStorage.clearTokens()
            AuthResult.Success(Unit)
        }
    }

    override suspend fun getCurrentUser(): AuthResult<User> {
        return try {
            val accessToken = tokenStorage.getAccessToken()
                ?: return AuthResult.Error("Не авторизован")

            val response = authApi.getCurrentUser(accessToken)

            val specialization = response.specialization?.let {
                Specialization(
                    id = it.id,
                    name = it.name,
                    description = it.description
                )
            }

            val skills = response.skills.mapIndexed { index, name ->
                Skill(id = index.toLong(), name = name)
            }

            AuthResult.Success(
                User(
                    id = response.id,
                    email = response.email,
                    nickname = response.nickname,
                    firstName = response.firstName,
                    lastName = response.lastName,
                    avatarUrl = response.avatarUrl,
                    createdAt = response.createdAt,
                    role = response.role,
                    specialization = specialization,
                    skills = skills,
                    bio = response.bio,
                    githubUrl = response.githubUrl,
                    telegramUsername = response.telegramUrl
                )
            )
        } catch (e: ClientRequestException) {
            if (e.response.status.value == 401) {
                AuthResult.Error("Сессия истекла")
            } else {
                AuthResult.Error("Ошибка загрузки профиля")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Ошибка подключения")
        }
    }

    override suspend fun refreshAccessToken(): AuthResult<String> {
        return try {
            val refreshToken = tokenStorage.getRefreshToken()
                ?: return AuthResult.Error("Refresh token не найден")

            val response = authApi.refreshToken(refreshToken)
            tokenStorage.saveAccessToken(response.token)

            AuthResult.Success(response.token)
        } catch (e: Exception) {
            AuthResult.Error("Не удалось обновить токен")
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