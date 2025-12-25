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
            println("Error loading teams: ${e.message}")
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
            println("Error creating team: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun getTeamDetails(teamId: String): Result<TeamDetails> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return Result.failure(Exception("Not authenticated"))

            val response = api.getTeamDetails(token, teamId)
            val details = response.toTeamDetails()
            Result.success(details)
        } catch (e: Exception) {
            println("Error loading team details: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun updateTeam(teamId: String, data: UpdateTeamData): Result<TeamDetails> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return Result.failure(Exception("Not authenticated"))

            val request = UpdateTeamRequest(
                name = data.name,
                description = data.description
            )

            val response = api.updateTeam(token, teamId, request)
            val details = response.toTeamDetails()
            Result.success(details)
        } catch (e: Exception) {
            println("Error updating team: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun joinTeam(inviteToken: String): Result<TeamDetails> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return Result.failure(Exception("Not authenticated"))

            val response = api.joinTeam(token, inviteToken)
            val details = response.toTeamDetails()
            Result.success(details)
        } catch (e: Exception) {
            println("Error joining team: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun leaveTeam(teamId: String): Result<Unit> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return Result.failure(Exception("Not authenticated"))

            api.leaveTeam(token, teamId)
            Result.success(Unit)
        } catch (e: Exception) {
            println("Error leaving team: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun deleteTeam(teamId: String): Result<Unit> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return Result.failure(Exception("Not authenticated"))

            api.deleteTeam(token, teamId)
            Result.success(Unit)
        } catch (e: Exception) {
            println("Error deleting team: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun kickMember(teamId: String, userId: String): Result<Unit> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return Result.failure(Exception("Not authenticated"))

            api.kickMember(token, teamId, userId)
            Result.success(Unit)
        } catch (e: Exception) {
            println("Error kicking member: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun updateMemberSpecialization(
        teamId: String,
        specializationId: Long?
    ): Result<TeamMember> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return Result.failure(Exception("Not authenticated"))

            val request = UpdateMemberSpecializationRequest(specializationId)
            val response = api.updateMemberSpecialization(token, teamId, request)
            val member = response.toTeamMember()
            Result.success(member)
        } catch (e: Exception) {
            println("Error updating specialization: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun regenerateInviteToken(teamId: String): Result<String> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return Result.failure(Exception("Not authenticated"))

            val response = api.regenerateInviteToken(token, teamId)
            val newToken = response["inviteToken"] ?: throw Exception("No token in response")
            Result.success(newToken)
        } catch (e: Exception) {
            println("Error regenerating token: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
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