package software.setixx.chimu.domain.usecase

import software.setixx.chimu.domain.model.RegisterTeam
import software.setixx.chimu.domain.model.UpdateRegistrationStatus
import software.setixx.chimu.domain.repository.TeamRegistrationRepository

class GetJamRegistrationsUseCase(
    private val repository: TeamRegistrationRepository
){
    suspend operator fun invoke(jamId: String) = repository.getJamRegistrations(jamId)
}

class RegisterTeamUseCase(
    private val repository: TeamRegistrationRepository
) {
    suspend operator fun invoke(teamId: RegisterTeam) = repository.registerTeam(teamId)
}

class WithdrawTeamUseCase(
    private val repository: TeamRegistrationRepository
){
    suspend operator fun invoke(jamId: String, teamId: String) = repository.withdrawTeam(jamId, teamId)
}

class UpdateRegistrationStatusUseCase(
    private val repository: TeamRegistrationRepository
){
    suspend operator fun invoke(jamId: String, teamId: String, status: UpdateRegistrationStatus) = repository.updateRegistrationStatus(jamId, teamId, status)
}