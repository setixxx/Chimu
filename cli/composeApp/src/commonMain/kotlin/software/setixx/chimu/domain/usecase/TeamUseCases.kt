package software.setixx.chimu.domain.usecase

import kotlinx.coroutines.flow.Flow
import software.setixx.chimu.domain.model.*
import software.setixx.chimu.domain.repository.TeamRepository

class ObserveUserTeamsUseCase(
    private val repository: TeamRepository
) {
    operator fun invoke(): Flow<List<Team>> = repository.teams
}

class GetTeamDetailsUseCase(
    private val repository: TeamRepository
) {
    suspend operator fun invoke(teamId: String) = repository.getTeamDetails(teamId)
}

class UpdateTeamUseCase(
    private val repository: TeamRepository
) {
    suspend operator fun invoke(teamId: String, data: UpdateTeam) = repository.updateTeam(teamId, data)
}

class JoinTeamUseCase(
    private val repository: TeamRepository
) {
    suspend operator fun invoke(inviteToken: String) = repository.joinTeam(inviteToken)
}

class LeaveTeamUseCase(
    private val repository: TeamRepository
) {
    suspend operator fun invoke(teamId: String) = repository.leaveTeam(teamId)
}

class DeleteTeamUseCase(
    private val repository: TeamRepository
) {
    suspend operator fun invoke(teamId: String) = repository.deleteTeam(teamId)
}

class KickMemberUseCase(
    private val repository: TeamRepository
) {
    suspend operator fun invoke(teamId: String, userId: String) = repository.kickMember(teamId, userId)
}

class UpdateMemberSpecializationUseCase(
    private val repository: TeamRepository
) {
    suspend operator fun invoke(teamId: String, specializationId: String?) = repository.updateMemberSpecialization(teamId, specializationId)
}

class RegenerateInviteTokenUseCase(
    private val repository: TeamRepository
) {
    suspend operator fun invoke(teamId: String) = repository.regenerateInviteToken(teamId)
}

class CreateTeamUseCase(
    private val repository: TeamRepository
) {
    suspend operator fun invoke(data: CreateTeam) = repository.createTeam(data)
}

class GetUserTeamsUseCase(
    private val repository: TeamRepository
) {
    suspend operator fun invoke() = repository.getUserTeams()
}