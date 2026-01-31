package software.setixx.chimu.data.repository

import software.setixx.chimu.data.local.TokenStorage
import software.setixx.chimu.data.remote.ProfileApi
import software.setixx.chimu.data.remote.dto.UpdateProfileRequest
import software.setixx.chimu.domain.model.*
import software.setixx.chimu.domain.repository.ProfileRepository

class ProfileRepositoryImpl(
    private val api: ProfileApi,
    private val tokenStorage: TokenStorage
) : ProfileRepository {

    override suspend fun updateProfile(request: ProfileUpdate): Result<User> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return Result.failure(Exception("Not authenticated"))

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

            Result.success(user)
        } catch (e: Exception) {
            println("Error updating profile: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
}