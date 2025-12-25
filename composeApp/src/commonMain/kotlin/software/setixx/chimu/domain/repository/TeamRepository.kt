package software.setixx.chimu.domain.repository

import software.setixx.chimu.domain.model.Team

interface TeamRepository {
    suspend fun getUserTeams(): Result<List<Team>>
}