package software.setixx.chimu.presentation.jam.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import software.setixx.chimu.api.domain.GameJamStatus
import software.setixx.chimu.api.domain.TransferStatus
import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.model.CreateJamTransfer
import software.setixx.chimu.domain.usecase.CancelJamUseCase
import software.setixx.chimu.domain.usecase.CancelTransferUseCase
import software.setixx.chimu.domain.usecase.CreateTransferUseCase
import software.setixx.chimu.domain.usecase.DeleteJamUseCase
import software.setixx.chimu.domain.usecase.ForceJamStatusUseCase
import software.setixx.chimu.domain.usecase.GetCurrentUserUseCase
import software.setixx.chimu.domain.usecase.GetJamDetailsUseCase
import software.setixx.chimu.domain.usecase.GetJamRegistrationsUseCase
import software.setixx.chimu.domain.usecase.GetTransferRequestsUseCase
import software.setixx.chimu.domain.usecase.GetUserByNicknameUseCase
import software.setixx.chimu.domain.usecase.GetUserTeamsUseCase

/**
 * ViewModel для детальной информации о Game Jam.
 * Отвечает за загрузку данных джема, управление регистрациями и передачу прав организатора.
 */
class JamDetailsViewModel(
    private val getJamDetailsUseCase: GetJamDetailsUseCase,
    private val deleteJamUseCase: DeleteJamUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val cancelJamUseCase: CancelJamUseCase,
    private val getTransferRequestsUseCase: GetTransferRequestsUseCase,
    private val createTransferUseCase: CreateTransferUseCase,
    private val cancelTransferUseCase: CancelTransferUseCase,
    private val getUserByNicknameUseCase: GetUserByNicknameUseCase,
    private val getUserTeamsUseCase: GetUserTeamsUseCase,
    private val getJamRegistrationsUseCase: GetJamRegistrationsUseCase,
    private val forceJamStatusUseCase: ForceJamStatusUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(JamDetailsState())
    val state: StateFlow<JamDetailsState> = _state.asStateFlow()

    fun loadJamDetails(jamId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            when (val userResult = getCurrentUserUseCase()) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            userRole = userResult.data.role,
                            userId = userResult.data.id
                        )
                    }
                }
                is ApiResult.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = userResult.message
                        )
                    }
                    return@launch
                }
            }

            when (val result = getJamDetailsUseCase(jamId)) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            jamDetails = result.data
                        )
                    }
                    loadRegistrationState(jamId)
                    loadCurrentTransfer(jamId)
                    _state.update { it.copy(isLoading = false) }
                }
                is ApiResult.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    private suspend fun loadRegistrationState(jamId: String) {
        when (val teamsResult = getUserTeamsUseCase()) {
            is ApiResult.Success -> _state.update { it.copy(userTeams = teamsResult.data) }
            is ApiResult.Error -> Unit
        }

        when (val registrationsResult = getJamRegistrationsUseCase(jamId)) {
            is ApiResult.Success -> _state.update { it.copy(registrations = registrationsResult.data) }
            is ApiResult.Error -> Unit
        }
    }

    private suspend fun loadCurrentTransfer(jamId: String) {
        when (val result = getTransferRequestsUseCase()) {
            is ApiResult.Success -> {
                val transfer = result.data.find {
                    it.jamId == jamId && it.status == TransferStatus.PENDING
                } ?: result.data.find { it.jamId == jamId }
                _state.update { it.copy(currentTransfer = transfer) }
            }
            is ApiResult.Error -> { }
        }
    }

    fun openForceStatusDialog(){
        _state.update {
            it.copy(
                showForceStatusDialog = true,
                selectedForceStatus = null,
                forceStatusError = null
            )
        }
    }

    fun closeForceStatusDialog(){
        _state.update {
            it.copy(
                showForceStatusDialog = false,
                selectedForceStatus = null,
                forceStatusError = null
            )
        }
    }

    fun forceJamStatus(jamId: String, targetStatus: GameJamStatus){
        viewModelScope.launch {
            _state.update { it.copy(isForceStatusActionIsLoading = true, forceStatusError = null) }
            when (val result = forceJamStatusUseCase(jamId, targetStatus)){
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            showForceStatusDialog = false,
                            selectedForceStatus = null,
                            isForceStatusActionIsLoading = false,
                            forceStatusError = null
                        )
                    }
                    loadJamDetails(jamId)
                }
                is ApiResult.Error -> {
                    _state.update {
                        it.copy(
                            forceStatusError = result.message,
                            isForceStatusActionIsLoading = false
                        )
                    }
                }
            }
        }

    }

    fun openTransferDialog() {
        _state.update {
            it.copy(
                showTransferDialog = true,
                transferError = null,
                transferRecipientQuery = "",
                transferRecipientFound = null
            )
        }
    }

    fun closeTransferDialog() {
        _state.update {
            it.copy(
                showTransferDialog = false,
                transferError = null,
                transferRecipientQuery = "",
                transferRecipientFound = null
            )
        }
    }

    fun onForceStatusSelected(status: GameJamStatus) {
        _state.update { it.copy(selectedForceStatus = status) }
    }

    fun onTransferRecipientQueryChange(query: String) {
        _state.update {
            it.copy(
                transferRecipientQuery = query,
                transferRecipientFound = null,
                transferError = null
            )
        }
    }

    fun searchRecipient() {
        val query = _state.value.transferRecipientQuery.trim()
        if (query.isBlank()) return
        viewModelScope.launch {
            _state.update { it.copy(isSearchingRecipient = true, transferError = null) }
            when (val result = getUserByNicknameUseCase(query)) {
                is ApiResult.Success -> {
                    val user = result.data
                    if (user.id == _state.value.userId) {
                        _state.update {
                            it.copy(
                                isSearchingRecipient = false,
                                transferError = "Нельзя передать джем самому себе"
                            )
                        }
                    } else {
                        _state.update {
                            it.copy(isSearchingRecipient = false, transferRecipientFound = user)
                        }
                    }
                }
                is ApiResult.Error -> _state.update {
                    it.copy(isSearchingRecipient = false, transferError = result.message)
                }
            }
        }
    }

    fun createTransfer(jamId: String) {
        val recipientId = _state.value.transferRecipientFound?.id ?: return
        viewModelScope.launch {
            _state.update { it.copy(isTransferActionLoading = true, transferError = null) }
            when (val result = createTransferUseCase(jamId, CreateJamTransfer(recipientId))) {
                is ApiResult.Success -> _state.update {
                    it.copy(
                        isTransferActionLoading = false,
                        currentTransfer = result.data,
                        showTransferDialog = false,
                        transferRecipientQuery = "",
                        transferRecipientFound = null
                    )
                }
                is ApiResult.Error -> _state.update {
                    it.copy(isTransferActionLoading = false, transferError = result.message)
                }
            }
        }
    }

    fun cancelTransfer(jamId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isTransferActionLoading = true, transferError = null) }
            when (val result = cancelTransferUseCase(jamId)) {
                is ApiResult.Success -> _state.update {
                    it.copy(
                        isTransferActionLoading = false,
                        currentTransfer = result.data,
                        showTransferDialog = false
                    )
                }
                is ApiResult.Error -> _state.update {
                    it.copy(isTransferActionLoading = false, transferError = result.message)
                }
            }
        }
    }

    fun deleteJam(jamId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isDeleting = true) }
            when (val result = deleteJamUseCase(jamId)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(isDeleting = false, isDeleted = true) }
                }
                is ApiResult.Error -> {
                    _state.update {
                        it.copy(
                            isDeleting = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun cancelJam(jamId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isCancelling = true) }
            when (val result = cancelJamUseCase(jamId)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(isCancelling = false, isCancelled = true) }
                }
                is ApiResult.Error -> {
                    _state.update {
                        it.copy(
                            isCancelling = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
}
