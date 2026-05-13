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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.usecase.GetAllJamsUseCase
import software.setixx.chimu.domain.usecase.GetCurrentUserUseCase
import software.setixx.chimu.domain.usecase.GetUserProjectsUseCase
import software.setixx.chimu.domain.usecase.GetUserTeamsUseCase
import software.setixx.chimu.domain.usecase.LogoutUseCase
import software.setixx.chimu.domain.usecase.ObserveJamsUseCase
import software.setixx.chimu.domain.usecase.ObserveUserTeamsUseCase
import software.setixx.chimu.domain.usecase.ObserverUserUseCase

class MainViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getActiveJamsUseCase: GetAllJamsUseCase,
    private val getUserTeamsUseCase: GetUserTeamsUseCase,
    private val getUserProjectsUseCase: GetUserProjectsUseCase,
    private val observeUserTeamsUseCase: ObserveUserTeamsUseCase,
    private val observerUserUseCase: ObserverUserUseCase,
    private val observeJamsUseCase: ObserveJamsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MainState())
    val state: StateFlow<MainState> = _state.asStateFlow()

    init {
        observerUser()
        observeTeams()
        observeJams()
        loadAllData()
    }

    private fun observeTeams() {
        viewModelScope.launch {
            observeUserTeamsUseCase().collectLatest { teams ->
                _state.update { it.copy(userTeams = teams) }
            }
        }
    }

    private fun observerUser(){
        viewModelScope.launch {
            observerUserUseCase().collectLatest { user ->
                _state.update { it.copy(user = user) }
            }
        }
    }

    private fun observeJams(){
        viewModelScope.launch {
            observeJamsUseCase().collectLatest { jams -> 
                _state.update { it.copy(activeJams = jams) }
            }
        }
    }

    private fun loadAllData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            loadUserData()
            loadActiveJams()
            loadUserTeams()
            loadUserProjects()
            loadNotifications()

            _state.update { it.copy(isLoading = false) }
        }
    }

    private suspend fun loadUserData() {
        when (val result = getCurrentUserUseCase()) {
            is ApiResult.Success -> {
                _state.update { it.copy(user = result.data) }
            }
            is ApiResult.Error -> {
                _state.update { it.copy(errorMessage = result.message) }
            }
        }
    }

    private suspend fun loadActiveJams() {
        when (val result = getActiveJamsUseCase()) {
            is ApiResult.Success -> {
                _state.update { it.copy(activeJams = result.data) }
            }
            is ApiResult.Error -> {
                _state.update {
                    it.copy(errorMessage = result.message)
                }
            }
        }
    }

    private suspend fun loadUserTeams() {
        when (val result = getUserTeamsUseCase()) {
            is ApiResult.Success -> {
                _state.update { it.copy(userTeams = result.data) }
            }
            is ApiResult.Error -> {
                _state.update {
                    it.copy(errorMessage = result.message)
                }
            }
        }
    }

    private suspend fun loadUserProjects() {
        when (val result = getUserProjectsUseCase()) {
            is ApiResult.Success -> {
                _state.update { it.copy(userProjects = result.data) }
            }
            is ApiResult.Error -> {
                _state.update {
                    it.copy(errorMessage = result.message)
                }
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

        _state.update {
            it.copy(
                notifications = mockNotifications,
                notificationCount = mockNotifications.size
            )
        }
    }

    fun onLogout(onLogoutSuccess: () -> Unit) {
        viewModelScope.launch {
            when (logoutUseCase()) {
                is ApiResult.Success -> {
                    onLogoutSuccess()
                }
                is ApiResult.Error -> {
                    _state.update {
                        it.copy(errorMessage = "Ошибка при выходе")
                    }
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    fun refresh() {
        loadAllData()
    }
}