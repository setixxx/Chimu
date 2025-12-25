package software.setixx.chimu.domain.usecase

import software.setixx.chimu.domain.model.CreateTeamData
import software.setixx.chimu.domain.model.Team
import software.setixx.chimu.domain.repository.TeamRepository

class CreateTeamUseCase(
    private val repository: TeamRepository
) {
    suspend operator fun invoke(data: CreateTeamData): Result<Team> {
        return repository.createTeam(data)
    }
}