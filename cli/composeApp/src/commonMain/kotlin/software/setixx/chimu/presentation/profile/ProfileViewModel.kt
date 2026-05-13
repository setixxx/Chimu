package software.setixx.chimu.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import software.setixx.chimu.domain.model.*
import software.setixx.chimu.domain.usecase.*

class ProfileViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val getAllSpecializationsUseCase: GetAllSpecializationsUseCase,
    private val getAllSkillsUseCase: GetAllSkillsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val availableSpecs = mutableListOf<Specialization>()
            val availableSkills = mutableListOf<Skill>()

            when (val result = getAllSpecializationsUseCase()) {
                is ApiResult.Success -> {
                    availableSpecs.addAll(result.data)
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

            when (val result = getAllSkillsUseCase()) {
                is ApiResult.Success -> {
                    availableSkills.addAll(result.data)
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

            when (val result = getCurrentUserUseCase()) {
                is ApiResult.Success -> {
                    val user = result.data

                    val currentSpec = user.specialization?.let { userSpec ->
                        availableSpecs.find { it.id == userSpec.id }
                    }

                    val currentSkills = user.skills!!.mapNotNull { userSkill ->
                        availableSkills.find { it.name == userSkill.name }
                    }

                    _state.update {
                        it.copy(
                            user = user,
                            nickname = user.nickname,
                            firstName = user.firstName ?: "",
                            lastName = user.lastName ?: "",
                            bio = user.bio ?: "",
                            githubUrl = user.githubUrl ?: "",
                            telegramUsername = user.telegramUrl ?: "",
                            selectedSpecialization = currentSpec,
                            selectedSkills = currentSkills,
                            availableSpecializations = availableSpecs,
                            availableSkills = availableSkills,
                            isLoading = false
                        )
                    }
                }
                is ApiResult.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message,
                            availableSpecializations = availableSpecs,
                            availableSkills = availableSkills
                        )
                    }
                }
            }
        }
    }

    fun toggleEditMode() {
        _state.update {
            it.copy(
                isEditing = !it.isEditing,
                nicknameError = null,
                githubError = null,
                telegramError = null
            )
        }
    }

    fun updateNickname(nickname: String) {
        _state.update {
            it.copy(
                nickname = nickname,
                nicknameError = null
            )
        }
    }

    fun updateFirstName(firstName: String) {
        _state.update { it.copy(firstName = firstName) }
    }

    fun updateLastName(lastName: String) {
        _state.update { it.copy(lastName = lastName) }
    }

    fun updateBio(bio: String) {
        _state.update { it.copy(bio = bio) }
    }

    fun updateGithubUrl(url: String) {
        _state.update {
            it.copy(
                githubUrl = url,
                githubError = null
            )
        }
    }

    fun updateTelegramUsername(username: String) {
        val cleaned = username.removePrefix("@")
        _state.update {
            it.copy(
                telegramUsername = cleaned,
                telegramError = null
            )
        }
    }

    fun updateSpecialization(spec: Specialization?) {
        _state.update { it.copy(selectedSpecialization = spec) }
    }

    fun toggleSkill(skill: Skill) {
        _state.update { currentState ->
            val currentSkills = currentState.selectedSkills
            val newSkills = if (currentSkills.contains(skill)) {
                currentSkills - skill
            } else {
                currentSkills + skill
            }
            currentState.copy(selectedSkills = newSkills)
        }
    }

    fun saveProfile() {
        if (!validateInputs()) return

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }

            val currentState = _state.value
            val request = ProfileUpdate(
                firstName = currentState.firstName.takeIf { it.isNotBlank() },
                lastName = currentState.lastName.takeIf { it.isNotBlank() },
                nickname = currentState.nickname.takeIf { it != currentState.user?.nickname },
                bio = currentState.bio.takeIf { it.isNotBlank() },
                specializationId = currentState.selectedSpecialization?.id,
                githubUrl = currentState.githubUrl.takeIf { it.isNotBlank() },
                telegramUsername = currentState.telegramUsername.takeIf { it.isNotBlank() },
                skillIds = currentState.selectedSkills.map { it.id }
            )

            when (val result = updateProfileUseCase(request)) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            user = result.data,
                            isSaving = false,
                            successMessage = "Профиль успешно обновлен"
                        )
                    }
                }
                is ApiResult.Error -> {
                    _state.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        val currentState = _state.value
        if (currentState.nickname.isBlank()) {
            _state.update { it.copy(nicknameError = "Никнейм не может быть пустым") }
            isValid = false
        } else if (!currentState.nickname.matches(Regex("^[a-zA-Z0-9_]+$"))) {
            _state.update {
                it.copy(
                    nicknameError = "Никнейм может содержать только буквы, цифры и _"
                )
            }
            isValid = false
        }

        if (currentState.githubUrl.isNotBlank() &&
            !currentState.githubUrl.matches(Regex("^https://github\\.com/[a-zA-Z0-9_-]+/?$"))
        ) {
            _state.update {
                it.copy(
                    githubError = "Неверный формат GitHub URL"
                )
            }
            isValid = false
        }

        if (currentState.telegramUsername.isNotBlank() &&
            !currentState.telegramUsername.matches(Regex("^[a-zA-Z0-9_]+$"))
        ) {
            _state.update {
                it.copy(
                    telegramError = "Username может содержать только буквы, цифры и _"
                )
            }
            isValid = false
        }

        return isValid
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    fun clearSuccess() {
        _state.update { it.copy(successMessage = null) }
    }
}