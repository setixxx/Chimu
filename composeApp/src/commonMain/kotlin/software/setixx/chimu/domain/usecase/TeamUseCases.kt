package software.setixx.chimu.domain.usecase

import software.setixx.chimu.domain.model.*
import software.setixx.chimu.domain.repository.TeamRepository

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
    suspend operator fun invoke(teamId: String, specializationId: Long?) = repository.updateMemberSpecialization(teamId, specializationId)
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