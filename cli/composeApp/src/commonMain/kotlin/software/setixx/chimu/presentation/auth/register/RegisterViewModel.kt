package software.setixx.chimu.presentation.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.usecase.RegisterUseCase
import software.setixx.chimu.presentation.utils.PasswordUtils

class RegisterViewModel(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> = _state.asStateFlow()

    fun onEmailChange(email: String) {
        _state.update {
            it.copy(
                email = email,
                emailError = null,
                errorMessage = null
            )
        }
    }

    fun onPasswordChange(password: String) {
        _state.update {
            it.copy(
                password = password,
                passwordError = null,
                errorMessage = null,
                passwordStrength = PasswordUtils.calculatePasswordStrength(password)
            )
        }
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _state.update {
            it.copy(
                confirmPassword = confirmPassword,
                confirmPasswordError = null,
                errorMessage = null
            )
        }
    }

    fun togglePasswordVisibility() {
        _state.update {
            it.copy(
                isPasswordVisible = !it.isPasswordVisible
            )
        }
    }

    fun toggleConfirmPasswordVisibility() {
        _state.update {
            it.copy(
                isConfirmPasswordVisible = !it.isConfirmPasswordVisible
            )
        }
    }

    fun onRegisterClick(onSuccess: () -> Unit) {
        if (!validateInput()) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = registerUseCase(
                email = _state.value.email.trim(),
                password = _state.value.password
            )) {
                is ApiResult.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    onSuccess()
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
        }
    }

    private fun validateInput(): Boolean {
        val email = _state.value.email.trim()
        val password = _state.value.password
        val confirmPassword = _state.value.confirmPassword

        var isValid = true

        if (email.isEmpty()) {
            _state.update { it.copy(emailError = "Email не может быть пустым") }
            isValid = false
        } else if (!isValidEmail(email)) {
            _state.update { it.copy(emailError = "Неверный формат email") }
            isValid = false
        }

        if (password.isEmpty()) {
            _state.update { it.copy(passwordError = "Пароль не может быть пустым") }
            isValid = false
        } else if (password.length < 8) {
            _state.update { it.copy(passwordError = "Пароль должен содержать минимум 8 символов") }
            isValid = false
        } else if (!isValidPassword(password)) {
            _state.update {
                it.copy(
                    passwordError = "Пароль должен содержать заглавные, строчные буквы и цифры"
                )
            }
            isValid = false
        }

        if (confirmPassword.isEmpty()) {
            _state.update { it.copy(confirmPasswordError = "Подтвердите пароль") }
            isValid = false
        } else if (password != confirmPassword) {
            _state.update { it.copy(confirmPasswordError = "Пароли не совпадают") }
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

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
}