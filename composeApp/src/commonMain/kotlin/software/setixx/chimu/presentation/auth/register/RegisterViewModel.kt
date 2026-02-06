package software.setixx.chimu.presentation.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.usecase.RegisterUseCase

class RegisterViewModel(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> = _state.asStateFlow()

    fun onEmailChange(email: String) {
        _state.value = _state.value.copy(
            email = email,
            emailError = null,
            errorMessage = null
        )
    }

    fun onPasswordChange(password: String) {
        _state.value = _state.value.copy(
            password = password,
            passwordError = null,
            errorMessage = null,
            passwordStrength = calculatePasswordStrength(password)
        )
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _state.value = _state.value.copy(
            confirmPassword = confirmPassword,
            confirmPasswordError = null,
            errorMessage = null
        )
    }

    fun togglePasswordVisibility() {
        _state.value = _state.value.copy(
            isPasswordVisible = !_state.value.isPasswordVisible
        )
    }

    fun toggleConfirmPasswordVisibility() {
        _state.value = _state.value.copy(
            isConfirmPasswordVisible = !_state.value.isConfirmPasswordVisible
        )
    }

    fun onRegisterClick(onSuccess: () -> Unit) {
        if (!validateInput()) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)

            when (val result = registerUseCase(
                email = _state.value.email.trim(),
                password = _state.value.password
            )) {
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
                else -> {}
            }
        }
    }

    private fun validateInput(): Boolean {
        val email = _state.value.email.trim()
        val password = _state.value.password
        val confirmPassword = _state.value.confirmPassword

        var isValid = true

        if (email.isEmpty()) {
            _state.value = _state.value.copy(emailError = "Email не может быть пустым")
            isValid = false
        } else if (!isValidEmail(email)) {
            _state.value = _state.value.copy(emailError = "Неверный формат email")
            isValid = false
        }

        if (password.isEmpty()) {
            _state.value = _state.value.copy(passwordError = "Пароль не может быть пустым")
            isValid = false
        } else if (password.length < 8) {
            _state.value = _state.value.copy(passwordError = "Пароль должен содержать минимум 8 символов")
            isValid = false
        } else if (!isValidPassword(password)) {
            _state.value = _state.value.copy(
                passwordError = "Пароль должен содержать заглавные, строчные буквы и цифры"
            )
            isValid = false
        }

        if (confirmPassword.isEmpty()) {
            _state.value = _state.value.copy(confirmPasswordError = "Подтвердите пароль")
            isValid = false
        } else if (password != confirmPassword) {
            _state.value = _state.value.copy(confirmPasswordError = "Пароли не совпадают")
            isValid = false
        }

        return isValid
    }

    private fun isValidEmail(email: String): Boolean {
        return email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
    }

    private fun isValidPassword(password: String): Boolean {
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        return hasUpperCase && hasLowerCase && hasDigit
    }

    private fun calculatePasswordStrength(password: String): PasswordStrength {
        if (password.length < 8) return PasswordStrength.WEAK

        var strength = 0
        if (password.any { it.isUpperCase() }) strength++
        if (password.any { it.isLowerCase() }) strength++
        if (password.any { it.isDigit() }) strength++
        if (password.any { !it.isLetterOrDigit() }) strength++
        if (password.length >= 12) strength++

        return when {
            strength <= 2 -> PasswordStrength.WEAK
            strength <= 4 -> PasswordStrength.MEDIUM
            else -> PasswordStrength.STRONG
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}