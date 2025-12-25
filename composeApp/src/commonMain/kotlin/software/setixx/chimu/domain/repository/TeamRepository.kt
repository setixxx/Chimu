package software.setixx.chimu.domain.repository

import software.setixx.chimu.domain.model.CreateTeamData
import software.setixx.chimu.domain.model.Team

interface TeamRepository {
    suspend fun getUserTeams(): Result<List<Team>>
    suspend fun createTeam(data: CreateTeamData): Result<Team>
}