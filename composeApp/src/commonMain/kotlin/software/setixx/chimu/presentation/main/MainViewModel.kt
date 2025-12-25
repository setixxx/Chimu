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
            is AuthResult.Success -> {
                _state.value = _state.value.copy(user = result.data)
            }
            is AuthResult.Error -> {
                _state.value = _state.value.copy(
                    errorMessage = result.message
                )
            }
            else -> {}
        }
    }

    private suspend fun loadActiveJams() {
        getActiveJamsUseCase().fold(
            onSuccess = { jams ->
                _state.value = _state.value.copy(activeJams = jams)
            },
            onFailure = { error ->
                _state.value = _state.value.copy(
                    errorMessage = error.message ?: "Failed to load jams"
                )
            }
        )
    }

    private suspend fun loadUserTeams() {
        getUserTeamsUseCase().fold(
            onSuccess = { teams ->
                _state.value = _state.value.copy(userTeams = teams)
            },
            onFailure = { error ->
            }
        )
    }

    private suspend fun loadUserProjects() {
        getUserProjectsUseCase().fold(
            onSuccess = { projects ->
                _state.value = _state.value.copy(userProjects = projects)
            },
            onFailure = { error ->
            }
        )
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

    fun refresh() {
        loadAllData()
    }
}