package software.setixx.chimu.domain.repository

import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.model.JamStatistics
import software.setixx.chimu.domain.model.Leaderboard

interface LeaderboardRepository {
    suspend fun getJamStatistics(jamId: String): ApiResult<JamStatistics>
    suspend fun getLeaderboard(jamId: String): ApiResult<Leaderboard>
}