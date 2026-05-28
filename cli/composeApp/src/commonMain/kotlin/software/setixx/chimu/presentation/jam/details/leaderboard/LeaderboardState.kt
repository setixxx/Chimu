package software.setixx.chimu.presentation.jam.details.leaderboard

import software.setixx.chimu.domain.model.GameJamDetails
import software.setixx.chimu.domain.model.JamStatistics
import software.setixx.chimu.domain.model.Leaderboard

/**
 * Состояние таблицы лидеров.
 * Хранит данные о позициях команд и итоговые показатели джема.
 */
data class LeaderboardState(
    val leaderboard: Leaderboard? = null,
    val statistics: JamStatistics? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)