package software.setixx.chimu.presentation.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Group
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import software.setixx.chimu.domain.model.AuthResult
import software.setixx.chimu.domain.usecase.GetCurrentUserUseCase
import software.setixx.chimu.domain.usecase.LogoutUseCase

class MainViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MainState())
    val state: StateFlow<MainState> = _state.asStateFlow()

    init {
        loadUserData()
        loadActiveJams()
        loadNotifications()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            when (val result = getCurrentUserUseCase()) {
                is AuthResult.Success -> {
                    _state.value = _state.value.copy(
                        user = result.data,
                        isLoading = false
                    )
                }
                is AuthResult.Error -> {
                    _state.value = _state.value.copy(
                        errorMessage = result.message,
                        isLoading = false
                    )
                }
                else -> {}
            }
        }
    }

    private fun loadActiveJams() {
        viewModelScope.launch {
            val mockJams = listOf(
                GameJamPreview(
                    id = "1",
                    name = "Winter Game Jam 2024",
                    theme = "Создание инновационных игровых механик",
                    status = "В процессе",
                    teamsCount = 15,
                    daysRemaining = 3
                ),
                GameJamPreview(
                    id = "2",
                    name = "Indie Showcase Jam",
                    theme = "Минимализм в дизайне",
                    status = "В процессе",
                    teamsCount = 20,
                    daysRemaining = 2
                ),
                GameJamPreview(
                    id = "3",
                    name = "New Year Game Jam",
                    theme = "Праздничная атмосфера",
                    status = "Регистрация",
                    teamsCount = 25,
                    daysRemaining = 1
                )
            )

            _state.value = _state.value.copy(
                activeJams = mockJams,
                isLoading = false
            )
        }
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            val mockNotifications = listOf(
                Notification(
                    id = "1",
                    message = "Новый джем начался!",
                    icon = Icons.Default.Event
                ),
                Notification(
                    id = "2",
                    message = "Приглашение в команду",
                    icon = Icons.Default.Group
                ),
                Notification(
                    id = "3",
                    message = "Новый комментарий",
                    icon = Icons.Default.Comment
                )
            )

            _state.value = _state.value.copy(
                notifications = mockNotifications,
                notificationCount = mockNotifications.size
            )
        }
    }

    fun onLogout(onLogoutSuccess: () -> Unit) {
        viewModelScope.launch {
            when (logoutUseCase()) {
                is AuthResult.Success -> {
                    onLogoutSuccess()
                }
                is AuthResult.Error -> {
                    _state.value = _state.value.copy(
                        errorMessage = "Ошибка при выходе"
                    )
                }
                else -> {}
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}