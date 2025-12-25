package software.setixx.chimu.data.repository

import software.setixx.chimu.data.local.TokenStorage
import software.setixx.chimu.data.remote.TeamApi
import software.setixx.chimu.data.remote.dto.CreateTeamRequest
import software.setixx.chimu.domain.model.CreateTeamData
import software.setixx.chimu.domain.model.Team
import software.setixx.chimu.domain.repository.TeamRepository

class TeamRepositoryImpl(
    private val api: TeamApi,
    private val tokenStorage: TokenStorage
) : TeamRepository {

    override suspend fun getUserTeams(): Result<List<Team>> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return Result.failure(Exception("Not authenticated"))

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
            Result.success(teams)
        } catch (e: Exception) {
            println("❌ Error loading teams: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun createTeam(data: CreateTeamData): Result<Team> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return Result.failure(Exception("Not authenticated"))

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

            Result.success(team)
        } catch (e: Exception) {
            println("❌ Error creating team: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
}