package software.setixx.chimu.data.repository

import software.setixx.chimu.data.local.TokenStorage
import software.setixx.chimu.data.remote.GameJamApi
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
            val jams = response.map { dto ->
                GameJam(
                    id = dto.id,
                    name = dto.name,
                    description = dto.description,
                    theme = dto.theme,
                    registrationStart = dto.registrationStart,
                    registrationEnd = dto.registrationEnd,
                    jamStart = dto.jamStart,
                    jamEnd = dto.jamEnd,
                    judgingStart = dto.judgingStart,
                    judgingEnd = dto.judgingEnd,
                    status = GameJamStatus.valueOf(dto.status),
                    organizerId = dto.organizerId,
                    organizerNickname = dto.organizerNickname,
                    registeredTeamsCount = dto.registeredTeamsCount,
                    maxTeamSize = dto.maxTeamSize,
                    minTeamSize = dto.minTeamSize,
                    createdAt = dto.createdAt
                )
            }
            ApiResult.Success(jams)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        } catch (e: IllegalArgumentException) {
            ApiResult.Error(e.message ?: "Неизвестная ошибка")
        }
    }

    override suspend fun getActiveJams(): ApiResult<List<GameJam>> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")

            val statuses = listOf("IN_PROGRESS", "REGISTRATION_OPEN", "JUDGING")
            val allJams = mutableListOf<GameJam>()

            for (status in statuses) {
                try {
                    val response = api.getJamsByStatus(status, token)
                    val jams = response.map { dto ->
                        GameJam(
                            id = dto.id,
                            name = dto.name,
                            description = dto.description,
                            theme = dto.theme,
                            registrationStart = dto.registrationStart,
                            registrationEnd = dto.registrationEnd,
                            jamStart = dto.jamStart,
                            jamEnd = dto.jamEnd,
                            judgingStart = dto.judgingStart,
                            judgingEnd = dto.judgingEnd,
                            status = GameJamStatus.valueOf(dto.status),
                            organizerId = dto.organizerId,
                            organizerNickname = dto.organizerNickname,
                            registeredTeamsCount = dto.registeredTeamsCount,
                            maxTeamSize = dto.maxTeamSize,
                            minTeamSize = dto.minTeamSize,
                            createdAt = dto.createdAt
                        )
                    }
                    allJams.addAll(jams)
                } catch (e: Exception) {
                    ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
                } catch (e: IllegalArgumentException) {
                    ApiResult.Error(e.message ?: "Неизвестная ошибка")
                }
            }

            ApiResult.Success(allJams)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        } catch (e: IllegalArgumentException) {
            ApiResult.Error(e.message ?: "Неизвестная ошибка")
        }
    }

    override suspend fun createJam(data: CreateGameJam): ApiResult<GameJamDetails> {
        TODO("Not yet implemented")
    }

    override suspend fun getJamDetails(gameJamId: String): ApiResult<GameJamDetails> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteJam(gameJamId: String): ApiResult<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun updateJam(
        gameJamId: String,
        data: UpdateGameJam,
    ): ApiResult<GameJamDetails> {
        TODO("Not yet implemented")
    }
}
