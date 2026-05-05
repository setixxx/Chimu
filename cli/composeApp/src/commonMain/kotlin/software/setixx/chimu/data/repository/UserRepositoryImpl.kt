package software.setixx.chimu.data.repository

import software.setixx.chimu.api.domain.UserRole
import software.setixx.chimu.data.local.TokenStorage
import software.setixx.chimu.data.remote.UserApi
import software.setixx.chimu.data.remote.dto.ChangePasswordRequest
import software.setixx.chimu.data.remote.dto.ChangePasswordResponse
import software.setixx.chimu.data.remote.dto.UpdateProfileRequest
import software.setixx.chimu.data.remote.dto.UserProfileResponse
import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.model.ChangePassword
import software.setixx.chimu.domain.model.ChangedPassword
import software.setixx.chimu.domain.model.ProfileUpdate
import software.setixx.chimu.domain.model.Skill
import software.setixx.chimu.domain.model.Specialization
import software.setixx.chimu.domain.model.UserProfile
import software.setixx.chimu.domain.repository.UserRepository

class UserRepositoryImpl(
    private val api: UserApi,
    private val tokenStorage: TokenStorage
) : UserRepository {

    override suspend fun changePassword(
        body: ChangePassword
    ): ApiResult<ChangedPassword> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")
            val request = ChangePasswordRequest(body.oldPassword, body.newPassword)
            val response = api.changePassword(token, request)
            ApiResult.Success(response.toDomain())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения")
        }
    }

    override suspend fun getCurrentUser(): ApiResult<UserProfile> {
        return try {
            val accessToken = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Не авторизован")

            val response = api.getCurrentUser(accessToken)
            ApiResult.Success(response.toDomain())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения")
        } catch (e: IllegalArgumentException) {
            ApiResult.Error(e.message ?: "Неизвестная ошибка")
        }
    }

    override suspend fun deleteProfile(): ApiResult<Unit> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")
            api.deleteProfile(token)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения")
        }
    }

    override suspend fun updateProfile(request: ProfileUpdate): ApiResult<UserProfile> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")

            val apiRequest = UpdateProfileRequest(
                firstName = request.firstName,
                lastName = request.lastName,
                nickname = request.nickname,
                bio = request.bio,
                specializationId = request.specializationId,
                githubUrl = request.githubUrl,
                telegramUsername = request.telegramUsername,
                skillIds = request.skillIds
            )

            val response = api.updateProfile(token, apiRequest)
            ApiResult.Success(response.toDomain())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения")
        } catch (e: IllegalArgumentException) {
            ApiResult.Error(e.message ?: "Неизвестная ошибка")
        }
    }

    override suspend fun getUserById(userId: String): ApiResult<UserProfile> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")
            val response = api.getUserById(token, userId)
            ApiResult.Success(response.toDomain())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения")
        }
    }

    private fun UserProfileResponse.toDomain(): UserProfile{
        val specialization = specialization?.let {
            Specialization(
                id = it.id,
                name = it.name,
                description = it.description
            )
        }
        return UserProfile(
            id = id,
            email = email,
            nickname = nickname,
            firstName = firstName,
            lastName = lastName,
            avatarUrl = avatarUrl,
            createdAt = createdAt,
            role = UserRole.valueOf(role),
            specialization = specialization,
            skills = skills.map { skill -> Skill(id = skill.id, name = skill.name) },
            bio = bio,
            githubUrl = githubUrl,
            telegramUrl = telegramUrl
        )
    }

    private fun ChangePasswordResponse.toDomain(): ChangedPassword{
        return ChangedPassword(
            message = message,
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }
}