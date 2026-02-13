package software.setixx.chimu.domain.repository

import software.setixx.chimu.domain.model.*

interface TeamRepository {
    suspend fun getUserTeams(): ApiResult<List<Team>>
    suspend fun createTeam(data: CreateTeam): ApiResult<Team>
    suspend fun getTeamDetails(teamId: String): ApiResult<TeamDetails>
    suspend fun updateTeam(teamId: String, data: UpdateTeam): ApiResult<TeamDetails>
    suspend fun joinTeam(inviteToken: String): ApiResult<TeamDetails>
    suspend fun leaveTeam(teamId: String): ApiResult<Unit>
    suspend fun deleteTeam(teamId: String): ApiResult<Unit>
    suspend fun kickMember(teamId: String, userId: String): ApiResult<Unit>
    suspend fun updateMemberSpecialization(teamId: String, specializationId: Long?): ApiResult<TeamMember>
    suspend fun regenerateInviteToken(teamId: String): ApiResult<String>
}