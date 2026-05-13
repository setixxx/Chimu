package software.setixx.chimu.presentation.jam.details.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.usecase.GetJamStatisticsUseCase
import software.setixx.chimu.domain.usecase.GetLeaderboardUseCase

class LeaderboardViewModel(
    private val getLeaderboardUseCase: GetLeaderboardUseCase,
    private val getJamStatisticsUseCase: GetJamStatisticsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LeaderboardState())
    val state: StateFlow<LeaderboardState> = _state.asStateFlow()

    fun load(jamId: String, loadStatistics: Boolean = false) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            when (val result = getLeaderboardUseCase(jamId)) {
                is ApiResult.Success -> _state.update { it.copy(leaderboard = result.data) }
                is ApiResult.Error -> _state.update { it.copy(errorMessage = result.message) }
            }

            if (loadStatistics) {
                when (val result = getJamStatisticsUseCase(jamId)) {
                    is ApiResult.Success -> _state.update { it.copy(statistics = result.data) }
                    is ApiResult.Error -> { /* statistics are optional – don't block */ }
                }
            }

            _state.update { it.copy(isLoading = false) }
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
}