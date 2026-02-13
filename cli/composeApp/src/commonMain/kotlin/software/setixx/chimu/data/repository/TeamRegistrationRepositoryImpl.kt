package software.setixx.chimu.data.repository

import software.setixx.chimu.data.local.TokenStorage
import software.setixx.chimu.data.remote.TeamRegistrationsApi
import software.setixx.chimu.data.remote.dto.RegisterTeamRequest
import software.setixx.chimu.data.remote.dto.RegistrationResponse
import software.setixx.chimu.data.remote.dto.UpdateRegistrationStatusRequest
import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.model.RegisterTeam
import software.setixx.chimu.domain.model.Registration
import software.setixx.chimu.domain.model.UpdateRegistrationStatus
import software.setixx.chimu.domain.repository.TeamRegistrationRepository

class TeamRegistrationRepositoryImpl(
    private val api: TeamRegistrationsApi,
    private val tokenStorage: TokenStorage
) : TeamRegistrationRepository {

    override suspend fun getJamRegistrations(jamId: String): ApiResult<List<Registration>> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")

            val response = api.getJamRegistrations(jamId, token)
            ApiResult.Success(response.map { it.toDomain() })
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    override suspend fun registerTeam(jamId: String, data: RegisterTeam): ApiResult<Registration> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")

            val request = RegisterTeamRequest(teamId = data.teamId)
            val response = api.registerTeam(jamId, token, request)
            ApiResult.Success(response.toDomain())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    override suspend fun withdrawTeam(jamId: String, teamId: String): ApiResult<Unit> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")

            api.withdrawTeam(jamId, teamId, token)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    override suspend fun updateRegistrationStatus(
        jamId: String,
        teamId: String,
        data: UpdateRegistrationStatus
    ): ApiResult<Unit> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")

            val request = UpdateRegistrationStatusRequest(status = data.status)
            api.updateRegistrationStatus(jamId, teamId, request, token)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    private fun RegistrationResponse.toDomain(): Registration {
        return Registration(
            id = id,
            jamId = jamId,
            jamName = jamName,
            teamId = teamId,
            teamName = teamName,
            status = status,
            registeredAt = registeredAt,
            registeredBy = registeredBy,
            registeredByNickname = registeredByNickname,
            updatedAt = updatedAt
        )
    }
}
