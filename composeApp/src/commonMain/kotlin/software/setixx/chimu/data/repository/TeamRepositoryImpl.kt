package software.setixx.chimu.data.repository

import software.setixx.chimu.data.local.TokenStorage
import software.setixx.chimu.data.remote.TeamApi
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
            Result.failure(e)
        }
    }
}