package software.setixx.chimu.presentation.profile.alien

import software.setixx.chimu.api.domain.UserRole
import software.setixx.chimu.domain.model.PublicUserProfile

/**
 * Состояние экрана просмотра чужого профиля.
 * Содержит публичные данные пользователя и статус загрузки.
 */
data class AlienProfileState(
    val profile: PublicUserProfile? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
){
    val role: String
        get() = when (profile?.role){
            UserRole.PARTICIPANT -> "Участник"
            UserRole.ORGANIZER -> "Организатор"
            UserRole.JUDGE -> "Судья"
            UserRole.ADMIN -> "Администратор"
            UserRole.GUEST -> "Гость"
            null -> ""
        }
}