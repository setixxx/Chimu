package software.setixx.chimu.presentation.team

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import software.setixx.chimu.domain.model.*
import software.setixx.chimu.domain.usecase.*

class TeamDetailsViewModel(
    private val getTeamDetailsUseCase: GetTeamDetailsUseCase,
    private val updateTeamUseCase: UpdateTeamUseCase,
    private val joinTeamUseCase: JoinTeamUseCase,
    private val leaveTeamUseCase: LeaveTeamUseCase,
    private val deleteTeamUseCase: DeleteTeamUseCase,
    private val kickMemberUseCase: KickMemberUseCase,
    private val updateMemberSpecializationUseCase: UpdateMemberSpecializationUseCase,
    private val regenerateInviteTokenUseCase: RegenerateInviteTokenUseCase,
    private val getAllSpecializationsUseCase: GetAllSpecializationsUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TeamDetailsState())
    val state: StateFlow<TeamDetailsState> = _state.asStateFlow()

    private var currentUserId: String? = null

    fun loadTeamDetails(teamId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            when (val userResult = getCurrentUserUseCase()) {
                is ApiResult.Success -> {
                    currentUserId = userResult.data.id
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = userResult.message
                    )
                }
            }

            when (val result = getTeamDetailsUseCase(teamId)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        team = result.data,
                        editName = result.data.name,
                        editDescription = result.data.description ?: "",
                        isLoading = false
                    )
                    loadSpecializations()
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    private fun loadSpecializations() {
        viewModelScope.launch {
            when (val result = getAllSpecializationsUseCase()) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        availableSpecializations = result.data
                    )
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun toggleEditMode() {
        if (_state.value.isEditing) {
            _state.value = _state.value.copy(
                isEditing = false,
                editName = _state.value.team?.name ?: "",
                editDescription = _state.value.team?.description ?: "",
                nameError = null
            )
        } else {
            _state.value = _state.value.copy(isEditing = true)
        }
    }

    fun updateName(name: String) {
        _state.value = _state.value.copy(
            editName = name,
            nameError = null
        )
    }

    fun updateDescription(description: String) {
        _state.value = _state.value.copy(editDescription = description)
    }

    fun saveTeam() {
        val team = _state.value.team ?: return

        if (!validateTeamData()) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true)

            val data = UpdateTeam(
                name = _state.value.editName.trim().takeIf { it != team.name },
                description = _state.value.editDescription.trim().takeIf { it != team.description }
            )

            when (val result = updateTeamUseCase(team.id, data)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        team = result.data,
                        editName = result.data.name,
                        editDescription = result.data.description ?: "",
                        isEditing = false,
                        isSaving = false,
                        successMessage = "Команда обновлена"
                    )
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(
                        isSaving = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    private fun validateTeamData(): Boolean {
        val name = _state.value.editName.trim()

        if (name.isBlank()) {
            _state.value = _state.value.copy(nameError = "Название не может быть пустым")
            return false
        }

        if (name.length < 3) {
            _state.value = _state.value.copy(nameError = "Название должно содержать минимум 3 символа")
            return false
        }

        if (name.length > 100) {
            _state.value = _state.value.copy(nameError = "Название не может превышать 100 символов")
            return false
        }

        if (!name.matches(Regex("^[a-zA-Z0-9]+( [a-zA-Z0-9]+)*$"))) {
            _state.value = _state.value.copy(nameError = "Название может содержать только буквы, цифры и пробелы")
            return false
        }

        return true
    }

    fun showLeaveDialog() {
        _state.value = _state.value.copy(showLeaveDialog = true)
    }

    fun hideLeaveDialog() {
        _state.value = _state.value.copy(showLeaveDialog = false)
    }

    fun leaveTeam(onSuccess: () -> Unit) {
        val team = _state.value.team ?: return

        viewModelScope.launch {
            hideLeaveDialog()
            _state.value = _state.value.copy(isLoading = true)

            when (val result = leaveTeamUseCase(team.id)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(isLoading = false)
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun showDeleteDialog() {
        _state.value = _state.value.copy(showDeleteDialog = true)
    }

    fun hideDeleteDialog() {
        _state.value = _state.value.copy(showDeleteDialog = false)
    }

    fun deleteTeam(onSuccess: () -> Unit) {
        val team = _state.value.team ?: return

        viewModelScope.launch {
            hideDeleteDialog()
            _state.value = _state.value.copy(isLoading = true)

            when (val result = deleteTeamUseCase(team.id)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(isLoading = false)
                    onSuccess()
                }

                is ApiResult.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun showKickDialog(member: TeamMember) {
        _state.value = _state.value.copy(
            showKickDialog = true,
            memberToKick = member
        )
    }

    fun hideKickDialog() {
        _state.value = _state.value.copy(
            showKickDialog = false,
            memberToKick = null
        )
    }

    fun kickMember() {
        val team = _state.value.team ?: return
        val member = _state.value.memberToKick ?: return

        viewModelScope.launch {
            hideKickDialog()

            when (val result = kickMemberUseCase(team.id, member.userId)) {
                is ApiResult.Success -> {
                    loadTeamDetails(team.id)
                    _state.value = _state.value.copy(
                        successMessage = "Участник исключен"
                    )
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun showInviteDialog() {
        _state.value = _state.value.copy(showInviteDialog = true)
    }

    fun hideInviteDialog() {
        _state.value = _state.value.copy(showInviteDialog = false)
    }

    fun regenerateToken() {
        val team = _state.value.team ?: return

        viewModelScope.launch {
            when (val result = regenerateInviteTokenUseCase(team.id)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        team = _state.value.team?.copy(inviteToken = result.data),
                        successMessage = "Токен обновлен"
                    )
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun showJoinDialog() {
        _state.value = _state.value.copy(showJoinDialog = true)
    }

    fun hideJoinDialog() {
        _state.value = _state.value.copy(
            showJoinDialog = false,
            inviteToken = ""
        )
    }

    fun updateInviteToken(token: String) {
        _state.value = _state.value.copy(inviteToken = token)
    }

    fun joinTeam(onSuccess: (String) -> Unit) {
        val token = _state.value.inviteToken.trim()

        if (token.isBlank()) {
            _state.value = _state.value.copy(errorMessage = "Введите токен приглашения")
            return
        }

        viewModelScope.launch {
            hideJoinDialog()
            _state.value = _state.value.copy(isLoading = true)

            when (val result = joinTeamUseCase(token)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(isLoading = false)
                    onSuccess(result.data.id)
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun showSpecializationDialog(currentSpec: Specialization?) {
        _state.value = _state.value.copy(
            showSpecializationDialog = true,
            selectedSpecialization = currentSpec
        )
    }

    fun hideSpecializationDialog() {
        _state.value = _state.value.copy(
            showSpecializationDialog = false,
            selectedSpecialization = null
        )
    }

    fun updateSpecialization(spec: Specialization?) {
        _state.value = _state.value.copy(selectedSpecialization = spec)
    }

    fun saveSpecialization() {
        val team = _state.value.team ?: return
        val specId = _state.value.selectedSpecialization?.id

        viewModelScope.launch {
            hideSpecializationDialog()

            when (val result = updateMemberSpecializationUseCase(team.id, specId)) {
                is ApiResult.Success -> {
                    loadTeamDetails(team.id)
                    _state.value = _state.value.copy(
                        successMessage = "Специализация обновлена"
                    )
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }

    fun clearSuccess() {
        _state.value = _state.value.copy(successMessage = null)
    }

    fun isCurrentUserLeader(): Boolean {
        return _state.value.team?.leaderId == currentUserId
    }

    fun getCurrentUserMember(): TeamMember? {
        return _state.value.team?.members?.find { it.userId == currentUserId }
    }
}