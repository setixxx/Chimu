package software.setixx.chimu.domain.usecase

import software.setixx.chimu.domain.model.*
import software.setixx.chimu.domain.repository.TeamRepository

class GetTeamDetailsUseCase(
    private val repository: TeamRepository
) {
    suspend operator fun invoke(teamId: String): Result<TeamDetails> {
        return repository.getTeamDetails(teamId)
    }
}

class UpdateTeamUseCase(
    private val repository: TeamRepository
) {
    suspend operator fun invoke(teamId: String, data: UpdateTeamData): Result<TeamDetails> {
        return repository.updateTeam(teamId, data)
    }
}

class JoinTeamUseCase(
    private val repository: TeamRepository
) {
    suspend operator fun invoke(inviteToken: String): Result<TeamDetails> {
        return repository.joinTeam(inviteToken)
    }
}

class LeaveTeamUseCase(
    private val repository: TeamRepository
) {
    suspend operator fun invoke(teamId: String): Result<Unit> {
        return repository.leaveTeam(teamId)
    }
}

class DeleteTeamUseCase(
    private val repository: TeamRepository
) {
    suspend operator fun invoke(teamId: String): Result<Unit> {
        return repository.deleteTeam(teamId)
    }
}

class KickMemberUseCase(
    private val repository: TeamRepository
) {
    suspend operator fun invoke(teamId: String, userId: String): Result<Unit> {
        return repository.kickMember(teamId, userId)
    }
}

class UpdateMemberSpecializationUseCase(
    private val repository: TeamRepository
) {
    suspend operator fun invoke(teamId: String, specializationId: Long?): Result<TeamMember> {
        return repository.updateMemberSpecialization(teamId, specializationId)
    }
}

class RegenerateInviteTokenUseCase(
    private val repository: TeamRepository
) {
    suspend operator fun invoke(teamId: String): Result<String> {
        return repository.regenerateInviteToken(teamId)
    }
}

class CreateTeamUseCase(
    private val repository: TeamRepository
) {
    suspend operator fun invoke(data: CreateTeamData): Result<Team> {
        return repository.createTeam(data)
    }
}