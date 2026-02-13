package software.setixx.chimu.presentation.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Group
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.http.cio.expectHttpBody
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.usecase.GetActiveJamsUseCase
import software.setixx.chimu.domain.usecase.GetCurrentUserUseCase
import software.setixx.chimu.domain.usecase.GetUserProjectsUseCase
import software.setixx.chimu.domain.usecase.GetUserTeamsUseCase
import software.setixx.chimu.domain.usecase.LogoutUseCase

class MainViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getActiveJamsUseCase: GetActiveJamsUseCase,
    private val getUserTeamsUseCase: GetUserTeamsUseCase,
    private val getUserProjectsUseCase: GetUserProjectsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MainState())
    val state: StateFlow<MainState> = _state.asStateFlow()

    init {
        loadAllData()
    }

    private fun loadAllData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            loadUserData()
            loadActiveJams()
            loadUserTeams()
            loadUserProjects()
            loadNotifications()

            _state.value = _state.value.copy(isLoading = false)
        }
    }

    private suspend fun loadUserData() {
        when (val result = getCurrentUserUseCase()) {
            is ApiResult.Success -> {
                _state.value = _state.value.copy(user = result.data)
            }
            is ApiResult.Error -> {
                _state.value = _state.value.copy(
                    errorMessage = result.message
                )
            }
        }
    }

    private suspend fun loadActiveJams() {
        when (val result = getActiveJamsUseCase()) {
            is ApiResult.Success -> {
                _state.value = _state.value.copy(activeJams = result.data)
            }
            is ApiResult.Error -> {
                _state.value = _state.value.copy(
                    errorMessage = result.message
                )
            }
        }
    }

    private suspend fun loadUserTeams() {
        when (val result = getUserTeamsUseCase()) {
            is ApiResult.Success -> {
                _state.value = _state.value.copy(userTeams = result.data)
            }
            is ApiResult.Error -> {
                _state.value = _state.value.copy(
                    errorMessage = result.message
                )
            }
        }
    }

    private suspend fun loadUserProjects() {
        when (val result = getUserProjectsUseCase()) {
            is ApiResult.Success -> {
                _state.value = _state.value.copy(userProjects = result.data)
            }
            is ApiResult.Error -> {
                _state.value = _state.value.copy(
                    errorMessage = result.message
                )
            }
        }
    }

    private fun loadNotifications() {
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

    fun onLogout(onLogoutSuccess: () -> Unit) {
        viewModelScope.launch {
            when (logoutUseCase()) {
                is ApiResult.Success -> {
                    onLogoutSuccess()
                }
                is ApiResult.Error -> {
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

    fun refresh() {
        loadAllData()
    }
}