package software.setixx.chimu.presentation.profile

import software.setixx.chimu.domain.model.Skill
import software.setixx.chimu.domain.model.Specialization
import software.setixx.chimu.domain.model.User

data class ProfileState(
    val user: User? = null,
    val nickname: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val bio: String = "",
    val githubUrl: String = "",
    val telegramUsername: String = "",
    val selectedSpecialization: Specialization? = null,
    val selectedSkills: List<Skill> = emptyList(),
    val availableSpecializations: List<Specialization> = emptyList(),
    val availableSkills: List<Skill> = emptyList(),
    val isEditing: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val nicknameError: String? = null,
    val githubError: String? = null,
    val telegramError: String? = null
)