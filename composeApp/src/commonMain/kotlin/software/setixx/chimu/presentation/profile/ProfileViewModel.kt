package software.setixx.chimu.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
            _state.value = _state.value.copy(isLoading = true)

            val availableSpecs = mutableListOf<Specialization>()
            val availableSkills = mutableListOf<Skill>()

            when (val result = getAllSpecializationsUseCase()) {
                is ApiResult.Success -> {
                    availableSpecs.addAll(result.data)
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }

            when (val result = getAllSkillsUseCase()) {
                is ApiResult.Success -> {
                    availableSkills.addAll(result.data)
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }

            when (val result = getCurrentUserUseCase()) {
                is ApiResult.Success -> {
                    val user = result.data

                    val currentSpec = user.specialization?.let { userSpec ->
                        availableSpecs.find { it.id == userSpec.id }
                    }

                    val currentSkills = user.skills.mapNotNull { userSkill ->
                        availableSkills.find { it.name == userSkill.name }
                    }

                    _state.value = _state.value.copy(
                        user = user,
                        nickname = user.nickname,
                        firstName = user.firstName ?: "",
                        lastName = user.lastName ?: "",
                        bio = user.bio ?: "",
                        githubUrl = user.githubUrl ?: "",
                        telegramUsername = user.telegramUsername ?: "",
                        selectedSpecialization = currentSpec,
                        selectedSkills = currentSkills,
                        availableSpecializations = availableSpecs,
                        availableSkills = availableSkills,
                        isLoading = false
                    )
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = result.message,
                        availableSpecializations = availableSpecs,
                        availableSkills = availableSkills
                    )
                }
            }
        }
    }

    fun toggleEditMode() {
        _state.value = _state.value.copy(
            isEditing = !_state.value.isEditing,
            nicknameError = null,
            githubError = null,
            telegramError = null
        )
    }

    fun updateNickname(nickname: String) {
        _state.value = _state.value.copy(
            nickname = nickname,
            nicknameError = null
        )
    }

    fun updateFirstName(firstName: String) {
        _state.value = _state.value.copy(firstName = firstName)
    }

    fun updateLastName(lastName: String) {
        _state.value = _state.value.copy(lastName = lastName)
    }

    fun updateBio(bio: String) {
        _state.value = _state.value.copy(bio = bio)
    }

    fun updateGithubUrl(url: String) {
        _state.value = _state.value.copy(
            githubUrl = url,
            githubError = null
        )
    }

    fun updateTelegramUsername(username: String) {
        val cleaned = username.removePrefix("@")
        _state.value = _state.value.copy(
            telegramUsername = cleaned,
            telegramError = null
        )
    }

    fun updateSpecialization(spec: Specialization?) {
        _state.value = _state.value.copy(selectedSpecialization = spec)
    }

    fun toggleSkill(skill: Skill) {
        val currentSkills = _state.value.selectedSkills
        val newSkills = if (currentSkills.contains(skill)) {
            currentSkills - skill
        } else {
            currentSkills + skill
        }
        _state.value = _state.value.copy(selectedSkills = newSkills)
    }

    fun saveProfile() {
        if (!validateInputs()) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true)

            val request = ProfileUpdate(
                firstName = _state.value.firstName.takeIf { it.isNotBlank() },
                lastName = _state.value.lastName.takeIf { it.isNotBlank() },
                nickname = _state.value.nickname.takeIf { it != _state.value.user?.nickname },
                bio = _state.value.bio.takeIf { it.isNotBlank() },
                specializationId = _state.value.selectedSpecialization?.id,
                githubUrl = _state.value.githubUrl.takeIf { it.isNotBlank() },
                telegramUsername = _state.value.telegramUsername.takeIf { it.isNotBlank() },
                skillIds = _state.value.selectedSkills.map { it.id }
            )

            when (val result = updateProfileUseCase(request)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        user = result.data,
                        isSaving = false,
                        successMessage = "Профиль успешно обновлен"
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

    private fun validateInputs(): Boolean {
        var isValid = true

        if (_state.value.nickname.isBlank()) {
            _state.value = _state.value.copy(nicknameError = "Никнейм не может быть пустым")
            isValid = false
        } else if (!_state.value.nickname.matches(Regex("^[a-zA-Z0-9_]+$"))) {
            _state.value = _state.value.copy(
                nicknameError = "Никнейм может содержать только буквы, цифры и _"
            )
            isValid = false
        }

        if (_state.value.githubUrl.isNotBlank() &&
            !_state.value.githubUrl.matches(Regex("^https://github\\.com/[a-zA-Z0-9_-]+/?$"))
        ) {
            _state.value = _state.value.copy(
                githubError = "Неверный формат GitHub URL"
            )
            isValid = false
        }

        if (_state.value.telegramUsername.isNotBlank() &&
            !_state.value.telegramUsername.matches(Regex("^[a-zA-Z0-9_]+$"))
        ) {
            _state.value = _state.value.copy(
                telegramError = "Username может содержать только буквы, цифры и _"
            )
            isValid = false
        }

        return isValid
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }

    fun clearSuccess() {
        _state.value = _state.value.copy(successMessage = null)
    }
}