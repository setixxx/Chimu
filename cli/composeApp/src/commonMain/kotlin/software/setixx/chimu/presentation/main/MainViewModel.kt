package software.setixx.chimu.presentation.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.http.cio.expectHttpBody
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import software.setixx.chimu.api.domain.TransferStatus
import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.model.ChangePassword
import software.setixx.chimu.domain.model.JamTransfer
import software.setixx.chimu.domain.model.ReviewJamTransfer
import software.setixx.chimu.domain.usecase.ChangePasswordUseCase
import software.setixx.chimu.domain.usecase.GetAllJamsUseCase
import software.setixx.chimu.domain.usecase.GetCurrentUserUseCase
import software.setixx.chimu.domain.usecase.GetJamJudgesUseCase
import software.setixx.chimu.domain.usecase.GetTransferRequestsUseCase
import software.setixx.chimu.domain.usecase.GetUserProjectsUseCase
import software.setixx.chimu.domain.usecase.GetUserTeamsUseCase
import software.setixx.chimu.domain.usecase.LogoutUseCase
import software.setixx.chimu.domain.usecase.ObserveJamsUseCase
import software.setixx.chimu.domain.usecase.ObserveUserTeamsUseCase
import software.setixx.chimu.domain.usecase.ObserverUserUseCase
import software.setixx.chimu.domain.usecase.ReviewTransferUseCase

/**
 * Главная ViewModel приложения.
 * Координирует загрузку данных пользователя, уведомлений и навигацию между основными разделами.
 */
class MainViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getActiveJamsUseCase: GetAllJamsUseCase,
    private val getUserTeamsUseCase: GetUserTeamsUseCase,
    private val getUserProjectsUseCase: GetUserProjectsUseCase,
    private val observeUserTeamsUseCase: ObserveUserTeamsUseCase,
    private val observerUserUseCase: ObserverUserUseCase,
    private val observeJamsUseCase: ObserveJamsUseCase,
    private val getTransferRequestsUseCase: GetTransferRequestsUseCase,
    private val reviewTransferUseCase: ReviewTransferUseCase,
    private val getJamJudgesUseCase: GetJamJudgesUseCase,
    private val changePasswordUseCase: ChangePasswordUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MainState())
    private var cachedIncomingTransfers: List<JamTransfer> = emptyList()
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
                loadJudgingJams()
            }
        }
    }

    private fun loadAllData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            loadUserData()
            loadActiveJams()
            loadJudgingJams()
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

    private suspend fun loadJudgingJams() {
        val currentUserId = _state.value.user?.id
        if (currentUserId == null) {
            _state.update { it.copy(judgingJams = emptyList()) }
            return
        }

        val judgingJams = _state.value.activeJams.filter { jam ->
            when (val result = getJamJudgesUseCase(jam.id)) {
                is ApiResult.Success -> result.data.any { judge -> judge.userId == currentUserId }
                is ApiResult.Error -> false
            }
        }

        _state.update { it.copy(judgingJams = judgingJams) }
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

    private suspend fun loadNotifications() {
        val currentUserId = _state.value.user?.id ?: return
        when (val result = getTransferRequestsUseCase()) {
            is ApiResult.Success -> {
                val incoming = result.data.filter {
                    it.status == TransferStatus.PENDING && it.recipientId == currentUserId
                }
                cachedIncomingTransfers = incoming
                val notifications = incoming.map { t ->
                    Notification(
                        id = t.id,
                        message = "«${t.jamName}»: ${t.senderNickname} предлагает вам стать организатором",
                        icon = Icons.Default.SwapHoriz,
                        actionType = NotificationActionType.JAM_TRANSFER_RECEIVED,
                        transferId = t.id
                    )
                }
                _state.update {
                    it.copy(notifications = notifications, notificationCount = notifications.size)
                }
            }
            is ApiResult.Error -> {
                _state.update { it.copy(notifications = emptyList(), notificationCount = 0) }
            }
        }
    }

    fun openTransferReview(transferId: String) {
        val transfer = cachedIncomingTransfers.find { it.id == transferId } ?: return
        _state.update { it.copy(pendingTransferToReview = transfer) }
    }

    fun closeTransferReview() {
        _state.update { it.copy(pendingTransferToReview = null, isReviewActionLoading = false) }
    }

    fun openChangePasswordDialog() {
        _state.update {
            it.copy(
                showChangePasswordDialog = true,
                changePasswordError = null
            )
        }
    }

    fun closeChangePasswordDialog() {
        _state.update {
            it.copy(
                showChangePasswordDialog = false,
                isChangePasswordLoading = false,
                changePasswordError = null
            )
        }
    }

    fun changePassword(oldPassword: String, newPassword: String, confirmPassword: String) {
        val validationError = validatePasswordChange(oldPassword, newPassword, confirmPassword)
        if (validationError != null) {
            _state.update { it.copy(changePasswordError = validationError) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isChangePasswordLoading = true, changePasswordError = null) }
            when (val result = changePasswordUseCase(ChangePassword(oldPassword, newPassword))) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            showChangePasswordDialog = false,
                            isChangePasswordLoading = false,
                            changePasswordError = null,
                            successMessage = "Пароль изменен"
                        )
                    }
                }
                is ApiResult.Error -> {
                    _state.update {
                        it.copy(
                            isChangePasswordLoading = false,
                            changePasswordError = result.message
                        )
                    }
                }
            }
        }
    }

    fun acceptTransfer(requestId: String) = doReview(requestId, accepted = true)
    fun rejectTransfer(requestId: String) = doReview(requestId, accepted = false)

    private fun doReview(requestId: String, accepted: Boolean) {
        viewModelScope.launch {
            _state.update { it.copy(isReviewActionLoading = true) }
            val status = if (accepted) TransferStatus.ACCEPTED else TransferStatus.REJECTED
            when (reviewTransferUseCase(requestId, ReviewJamTransfer(status))) {
                is ApiResult.Success -> {
                    _state.update { it.copy(isReviewActionLoading = false, pendingTransferToReview = null) }
                    loadNotifications()
                }
                is ApiResult.Error -> {
                    _state.update { it.copy(isReviewActionLoading = false) }
                }
            }
        }
    }

    fun onLogout(onLogoutSuccess: () -> Unit) {
        viewModelScope.launch {
            when (logoutUseCase()) {
                is ApiResult.Success -> {
                    onLogoutSuccess()
                }
                is ApiResult.Error -> {
                    onLogoutSuccess()
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    fun clearSuccess() {
        _state.update { it.copy(successMessage = null) }
    }

    fun refresh() {
        loadAllData()
    }

    private fun validatePasswordChange(
        oldPassword: String,
        newPassword: String,
        confirmPassword: String
    ): String? {
        return when {
            oldPassword.isBlank() -> "Введите текущий пароль"
            newPassword.isBlank() -> "Введите новый пароль"
            newPassword.length < 8 -> "Новый пароль должен содержать минимум 8 символов"
            newPassword == oldPassword -> "Новый пароль должен отличаться от текущего"
            newPassword != confirmPassword -> "Пароли не совпадают"
            else -> null
        }
    }
}
