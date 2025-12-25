package software.setixx.chimu.domain.repository

import software.setixx.chimu.domain.model.*

interface TeamRepository {
    suspend fun getUserTeams(): Result<List<Team>>
    suspend fun createTeam(data: CreateTeamData): Result<Team>
    suspend fun getTeamDetails(teamId: String): Result<TeamDetails>
    suspend fun updateTeam(teamId: String, data: UpdateTeamData): Result<TeamDetails>
    suspend fun joinTeam(inviteToken: String): Result<TeamDetails>
    suspend fun leaveTeam(teamId: String): Result<Unit>
    suspend fun deleteTeam(teamId: String): Result<Unit>
    suspend fun kickMember(teamId: String, userId: String): Result<Unit>
    suspend fun updateMemberSpecialization(teamId: String, specializationId: Long?): Result<TeamMember>
    suspend fun regenerateInviteToken(teamId: String): Result<String>
}