package software.setixx.chimu.presentation.jam.create

import software.setixx.chimu.api.domain.UserRole

/**
 * Состояние экрана создания Game Jam.
 * Хранит временные данные полей ввода и ошибки валидации.
 */
data class CreateJamState(
    val name: String = "",
    val description: String = "",
    val theme: String = "",
    val rules: String = "",
    val registrationStart: String = "",
    val registrationEnd: String = "",
    val jamStart: String = "",
    val jamEnd: String = "",
    val judgingStart: String = "",
    val judgingEnd: String = "",
    val minTeamSize: String = "1",
    val maxTeamSize: String = "5",
    
    val nameError: String? = null,
    val descriptionError: String? = null,
    val themeError: String? = null,
    val rulesError: String? = null,
    val dateError: String? = null,
    val teamSizeError: String? = null,
    
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val createdJamId: String? = null,
    val errorMessage: String? = null,
    val userRole: UserRole? = null
)
