package software.setixx.chimu.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import software.setixx.chimu.domain.usecase.CheckAuthStatusUseCase

class SplashViewModel(
    private val checkAuthStatusUseCase: CheckAuthStatusUseCase
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun checkAuthStatus(
        onAuthenticated: () -> Unit,
        onNotAuthenticated: () -> Unit
    ) {
        viewModelScope.launch {
            val isLoggedIn = checkAuthStatusUseCase()
            _isLoading.value = false

            if (isLoggedIn) {
                onAuthenticated()
            } else {
                onNotAuthenticated()
            }
        }
    }
}