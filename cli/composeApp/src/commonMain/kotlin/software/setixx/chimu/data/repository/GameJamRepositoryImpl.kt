package software.setixx.chimu.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import software.setixx.chimu.api.domain.GameJamStatus
import software.setixx.chimu.data.local.TokenStorage
import software.setixx.chimu.data.remote.GameJamApi
import software.setixx.chimu.data.remote.dto.CreateGameJamRequest
import software.setixx.chimu.data.remote.dto.ForceJamStatusRequest
import software.setixx.chimu.data.remote.dto.GameJamDetailsResponse
import software.setixx.chimu.data.remote.dto.GameJamResponse
import software.setixx.chimu.data.remote.dto.UpdateGameJamRequest
import software.setixx.chimu.data.util.getBaseUrl
import software.setixx.chimu.domain.model.*
import software.setixx.chimu.domain.repository.GameJamRepository

/**
 * Реализация репозитория игровых джемов, обеспечивающая доступ к данным через API и хранение состояния.
 */
class GameJamRepositoryImpl(
    private val api: GameJamApi,
    private val tokenStorage: TokenStorage
) : GameJamRepository {
    private val _allJams = MutableStateFlow<List<GameJam>>(emptyList())
    override val allJams: Flow<List<GameJam>> = _allJams.asStateFlow()

    private val _selectedJam = MutableStateFlow<GameJamDetails?>(null)
    override val selectedJam: Flow<GameJamDetails?> = _selectedJam.asStateFlow()

    override suspend fun getAllJams(): ApiResult<List<GameJam>> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")

            val response = api.getAllJams(token)
            _allJams.value = response.map { it.toDomain() }
            ApiResult.Success(response.map { it.toDomain() })
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    override suspend fun getJamDetails(gameJamId: String): ApiResult<GameJamDetails> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")

            val response = api.getJamDetails(gameJamId, token)
            _selectedJam.value = response.toDomain()
            ApiResult.Success(response.toDomain())
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
            getAllJams()
            ApiResult.Success(response.toDomain())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    override suspend fun cancelJam(gameJamId: String): ApiResult<GameJamDetails> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")
            val response = api.cancelJam(gameJamId, token)
            getAllJams()
            getJamDetails(gameJamId)
            ApiResult.Success(response.toDomain())
        } catch (e: Exception){
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    override suspend fun deleteJam(gameJamId: String): ApiResult<Unit> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")

            api.deleteJam(gameJamId, token)
            getAllJams()
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
            getAllJams()
            ApiResult.Success(response.toDomain())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    override suspend fun forceJamStatus(
        jamId: String,
        targetStatus: GameJamStatus
    ): ApiResult<GameJamDetails> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")

            val request = ForceJamStatusRequest(status = targetStatus)

            val response = api.forceJamStatus(jamId, request, token)
            getAllJams()
            getJamDetails(jamId)
            ApiResult.Success(response.toDomain())
        } catch (e: Exception){
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
            bannerUrl = if (bannerUrl != null) {
                val version = updatedAt.hashCode()
                "${getBaseUrl()}/api/jams/$id/banner?v=$version"
            } else null,
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
            status = GameJamStatus.valueOf(status),
            organizerId = organizerId,
            organizerNickname = organizerNickname,
            minTeamSize = minTeamSize,
            maxTeamSize = maxTeamSize,
            bannerUrl = if (bannerUrl != null) {
                val version = updatedAt.hashCode()
                "${getBaseUrl()}/api/jams/$id/banner?v=$version"
            } else null,
            createdAt = createdAt,
            updatedAt = updatedAt,
            criteria = criteria,
            judges = judges,
            registeredTeamsCount = registeredTeamsCount,
            submittedProjectsCount = submittedProjectsCount
        )
    }
}
