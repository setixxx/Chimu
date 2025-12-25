package software.setixx.chimu.domain.usecase

import software.setixx.chimu.domain.model.Team
import software.setixx.chimu.domain.repository.TeamRepository

class GetUserTeamsUseCase(
    private val repository: TeamRepository
) {
    suspend operator fun invoke(): Result<List<Team>> {
        return repository.getUserTeams()
    }
}