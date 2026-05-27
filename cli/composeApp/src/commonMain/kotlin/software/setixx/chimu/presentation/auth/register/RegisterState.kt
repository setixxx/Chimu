package software.setixx.chimu.presentation.auth.register

import software.setixx.chimu.presentation.utils.PasswordStrength

data class RegisterState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val passwordStrength: PasswordStrength = PasswordStrength.WEAK
)
