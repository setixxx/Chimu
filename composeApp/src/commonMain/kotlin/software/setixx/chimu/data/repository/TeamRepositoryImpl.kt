package software.setixx.chimu.data.repository

import software.setixx.chimu.data.local.TokenStorage
import software.setixx.chimu.data.remote.TeamApi
import software.setixx.chimu.data.remote.dto.*
import software.setixx.chimu.domain.model.*
import software.setixx.chimu.domain.repository.TeamRepository

class TeamRepositoryImpl(
    private val api: TeamApi,
    private val tokenStorage: TokenStorage
) : TeamRepository {

    override suspend fun getUserTeams(): ApiResult<List<Team>> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")

            val response = api.getUserTeams(token)
            val teams = response.map { dto ->
                Team(
                    id = dto.id,
                    name = dto.name,
                    description = dto.description,
                    memberCount = dto.memberCount,
                    isLeader = dto.isLeader,
                    createdAt = dto.createdAt
                )
            }
            ApiResult.Success(teams)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    override suspend fun createTeam(data: CreateTeam): ApiResult<Team> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")

            val request = CreateTeamRequest(
                name = data.name,
                description = data.description
            )

            val response = api.createTeam(token, request)

            val team = Team(
                id = response.id,
                name = response.name,
                description = response.description,
                memberCount = response.members.size,
                isLeader = response.members.any { it.isLeader },
                createdAt = response.createdAt
            )

            ApiResult.Success(team)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    override suspend fun getTeamDetails(teamId: String): ApiResult<TeamDetails> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")

            val response = api.getTeamDetails(token, teamId)
            val details = response.toTeamDetails()
            ApiResult.Success(details)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    override suspend fun updateTeam(teamId: String, data: UpdateTeam): ApiResult<TeamDetails> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")

            val request = UpdateTeamRequest(
                name = data.name,
                description = data.description
            )

            val response = api.updateTeam(token, teamId, request)
            val details = response.toTeamDetails()
            ApiResult.Success(details)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    override suspend fun joinTeam(inviteToken: String): ApiResult<TeamDetails> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")

            val response = api.joinTeam(token, inviteToken)
            val details = response.toTeamDetails()
            ApiResult.Success(details)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    override suspend fun leaveTeam(teamId: String): ApiResult<Unit> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")

            api.leaveTeam(token, teamId)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    override suspend fun deleteTeam(teamId: String): ApiResult<Unit> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")

            api.deleteTeam(token, teamId)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    override suspend fun kickMember(teamId: String, userId: String): ApiResult<Unit> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")

            api.kickMember(token, teamId, userId)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    override suspend fun updateMemberSpecialization(
        teamId: String,
        specializationId: Long?
    ): ApiResult<TeamMember> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")

            val request = UpdateMemberSpecializationRequest(specializationId)
            val response = api.updateMemberSpecialization(token, teamId, request)
            val member = response.toTeamMember()
            ApiResult.Success(member)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    override suspend fun regenerateInviteToken(teamId: String): ApiResult<String> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")

            val response = api.regenerateInviteToken(token, teamId)
            val newToken = response["inviteToken"] ?: throw Exception("No token in response")
            ApiResult.Success(newToken)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    private fun TeamDetailsResponse.toTeamDetails() = TeamDetails(
        id = id,
        name = name,
        description = description,
        leaderId = leaderId,
        inviteToken = inviteToken,
        createdAt = createdAt,
        members = members.map { it.toTeamMember() }
    )

    private fun TeamMemberResponse.toTeamMember() = TeamMember(
        userId = userId,
        nickname = nickname,
        avatarUrl = avatarUrl,
        specialization = specialization?.let {
            Specialization(
                id = it.id,
                name = it.name,
                description = it.description
            )
        },
        joinedAt = joinedAt,
        isLeader = isLeader
    )
}