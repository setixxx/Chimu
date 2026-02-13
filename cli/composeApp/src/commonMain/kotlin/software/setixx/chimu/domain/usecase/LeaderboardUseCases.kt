package software.setixx.chimu.domain.usecase

import software.setixx.chimu.domain.repository.LeaderboardRepository

class GetJamStatisticsUseCase(
    private val repository: LeaderboardRepository
) {
    suspend operator fun invoke(jamId: String) = repository.getJamStatistics(jamId)
}

class GetLeaderboardUseCase(
    private val repository: LeaderboardRepository
) {
    suspend operator fun invoke(jamId: String) = repository.getLeaderboard(jamId)
}