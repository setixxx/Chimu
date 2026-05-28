package software.setixx.chimu.presentation.profile.own

import software.setixx.chimu.api.domain.UserRole
import software.setixx.chimu.domain.model.Skill
import software.setixx.chimu.domain.model.Specialization
import software.setixx.chimu.domain.model.UserProfile

/**
 * Состояние экрана собственного профиля.
 * Хранит текущие и редактируемые данные пользователя, а также списки доступных навыков.
 */
data class OwnProfileState(
    val user: UserProfile? = null,
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
){
    val role: String
        get() = when (user?.role){
            UserRole.PARTICIPANT -> "Участник"
            UserRole.ORGANIZER -> "Организатор"
            UserRole.JUDGE -> "Судья"
            UserRole.ADMIN -> "Администратор"
            UserRole.GUEST -> "Гость"
            null -> ""
        }
}