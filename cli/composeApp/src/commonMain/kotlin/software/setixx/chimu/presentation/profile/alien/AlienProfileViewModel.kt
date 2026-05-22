package software.setixx.chimu.presentation.profile.alien

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.usecase.GetUserByIdUseCase

class AlienProfileViewModel(
    private val userId: String,
    private val getUserByIdUseCase: GetUserByIdUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AlienProfileState())
    val state: StateFlow<AlienProfileState> = _state.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getUserByIdUseCase(userId)) {
                is ApiResult.Success -> {
                    println(result.data)
                    _state.update { it.copy(profile = result.data, isLoading = false) }
                }
                is ApiResult.Error -> {
                    _state.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
}