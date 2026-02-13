package software.setixx.chimu.data.repository

import software.setixx.chimu.data.local.TokenStorage
import software.setixx.chimu.data.remote.GameJamApi
import software.setixx.chimu.data.remote.dto.CreateGameJamRequest
import software.setixx.chimu.data.remote.dto.GameJamDetailsResponse
import software.setixx.chimu.data.remote.dto.GameJamResponse
import software.setixx.chimu.data.remote.dto.UpdateGameJamRequest
import software.setixx.chimu.domain.model.*
import software.setixx.chimu.domain.repository.GameJamRepository

class GameJamRepositoryImpl(
    private val api: GameJamApi,
    private val tokenStorage: TokenStorage
) : GameJamRepository {

    override suspend fun getAllJams(): ApiResult<List<GameJam>> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")

            val response = api.getAllJams(token)
            ApiResult.Success(response.map { it.toDomain() })
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    override suspend fun getActiveJams(): ApiResult<List<GameJam>> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")

            val statuses = listOf("IN_PROGRESS", "REGISTRATION_OPEN", "JUDGING")
            val allJams = mutableListOf<GameJam>()

            for (status in statuses) {
                val response = api.getJamsByStatus(status, token)
                allJams.addAll(response.map { it.toDomain() })
            }

            ApiResult.Success(allJams)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    override suspend fun createJam(data: CreateGameJam): ApiResult<GameJamDetails> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")

            val request = CreateGameJamRequest(
                name = data.name,
                description = data.description,
                theme = data.theme,
                rules = data.rules,
                registrationStart = data.registrationStart,
                registrationEnd = data.registrationEnd,
                jamStart = data.jamStart,
                jamEnd = data.jamEnd,
                judgingStart = data.judgingStart,
                judgingEnd = data.judgingEnd,
                minTeamSize = data.minTeamSize,
                maxTeamSize = data.maxTeamSize
            )

            val response = api.createJam(request, token)
            ApiResult.Success(response.toDomain())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    override suspend fun getJamDetails(gameJamId: String): ApiResult<GameJamDetails> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")

            val response = api.getJamDetails(gameJamId, token)
            ApiResult.Success(response.toDomain())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    override suspend fun deleteJam(gameJamId: String): ApiResult<Unit> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")

            api.deleteJam(gameJamId, token)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    override suspend fun updateJam(
        gameJamId: String,
        data: UpdateGameJam,
    ): ApiResult<GameJamDetails> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")

            val request = UpdateGameJamRequest(
                name = data.name,
                description = data.description,
                theme = data.theme,
                rules = data.rules,
                registrationStart = data.registrationStart,
                registrationEnd = data.registrationEnd,
                jamStart = data.jamStart,
                jamEnd = data.jamEnd,
                judgingStart = data.judgingStart,
                judgingEnd = data.judgingEnd,
                minTeamSize = data.minTeamSize,
                maxTeamSize = data.maxTeamSize
            )

            val response = api.updateJam(gameJamId, request, token)
            ApiResult.Success(response.toDomain())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    private fun GameJamResponse.toDomain(): GameJam {
        return GameJam(
            id = id,
            name = name,
            description = description,
            theme = theme,
            registrationStart = registrationStart,
            registrationEnd = registrationEnd,
            jamStart = jamStart,
            jamEnd = jamEnd,
            judgingStart = judgingStart,
            judgingEnd = judgingEnd,
            status = try {
                GameJamStatus.valueOf(status)
            } catch (e: Exception) {
                GameJamStatus.CANCELLED
            },
            organizerId = organizerId,
            organizerNickname = organizerNickname,
            registeredTeamsCount = registeredTeamsCount,
            maxTeamSize = maxTeamSize,
            minTeamSize = minTeamSize,
            createdAt = createdAt
        )
    }

    private fun GameJamDetailsResponse.toDomain(): GameJamDetails {
        return GameJamDetails(
            id = id,
            name = name,
            description = description,
            theme = theme,
            rules = rules,
            registrationStart = registrationStart,
            registrationEnd = registrationEnd,
            jamStart = jamStart,
            jamEnd = jamEnd,
            judgingStart = judgingStart,
            judgingEnd = judgingEnd,
            status = status,
            organizerId = organizerId,
            organizerNickname = organizerNickname,
            minTeamSize = minTeamSize,
            maxTeamSize = maxTeamSize,
            createdAt = createdAt,
            updatedAt = updatedAt,
            criteria = criteria,
            judges = judges,
            registeredTeamsCount = registeredTeamsCount,
            submittedProjectsCount = submittedProjectsCount
        )
    }
}
