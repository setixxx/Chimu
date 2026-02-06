package software.setixx.chimu.data.repository

import software.setixx.chimu.data.local.TokenStorage
import software.setixx.chimu.data.remote.ProfileApi
import software.setixx.chimu.data.remote.dto.UpdateProfileRequest
import software.setixx.chimu.domain.model.*
import software.setixx.chimu.domain.repository.UserRepository

class UserRepositoryImpl(
    private val api: ProfileApi,
    private val tokenStorage: TokenStorage
) : UserRepository {

    override suspend fun updateProfile(request: ProfileUpdate): ApiResult<User> {
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

            val user = User(
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

            ApiResult.Success(user)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения")
        } catch (e: IllegalArgumentException) {
            ApiResult.Error(e.message ?: "Неизвестная ошибка")
        }
    }

    override suspend fun getCurrentUser(): ApiResult<User> {
        return try {
            val accessToken = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Не авторизован")

            val response = api.getCurrentUser(accessToken)

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

            ApiResult.Success(
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
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения")
        } catch (e: IllegalArgumentException) {
            ApiResult.Error(e.message ?: "Неизвестная ошибка")
        }
    }
}