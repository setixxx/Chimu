package software.setixx.chimu.presentation.auth.login

/**
 * Состояние пользовательского интерфейса экрана входа.
 * Содержит данные полей ввода, ошибки и статус загрузки.
 */
data class LoginState(
    val email: String = "",
    val password: String = "",
    val rememberMe: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isPasswordVisible: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null
)