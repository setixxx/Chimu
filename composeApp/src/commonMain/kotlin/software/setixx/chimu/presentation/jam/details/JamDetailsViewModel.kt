package software.setixx.chimu.presentation.jam.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.usecase.DeleteJamUseCase
import software.setixx.chimu.domain.usecase.GetCurrentUserUseCase
import software.setixx.chimu.domain.usecase.GetJamDetailsUseCase

class JamDetailsViewModel(
    private val getJamDetailsUseCase: GetJamDetailsUseCase,
    private val deleteJamUseCase: DeleteJamUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(JamDetailsState())
    val state: StateFlow<JamDetailsState> = _state.asStateFlow()

    fun loadJamDetails(jamId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            val userResult = getCurrentUserUseCase()
            if (userResult is ApiResult.Success) {
                _state.value = _state.value.copy(
                    userRole = userResult.data.role,
                    userId = userResult.data.id
                )
            }

            when (val result = getJamDetailsUseCase(jamId)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        jamDetails = result.data,
                        isLoading = false
                    )
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(
                        errorMessage = result.message,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun deleteJam(jamId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isDeleting = true)
            when (val result = deleteJamUseCase(jamId)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        isDeleting = false,
                        isDeleted = true
                    )
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(
                        isDeleting = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}