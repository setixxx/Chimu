package software.setixx.chimu.presentation.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import software.setixx.chimu.domain.model.AuthResult
import software.setixx.chimu.domain.usecase.GetSavedEmailUseCase
import software.setixx.chimu.domain.usecase.LoginUseCase

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val getSavedEmailUseCase: GetSavedEmailUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    init {
        loadSavedEmail()
    }

    private fun loadSavedEmail() {
        viewModelScope.launch {
            val savedEmail = getSavedEmailUseCase()
            if (savedEmail != null) {
                _state.value = _state.value.copy(
                    email = savedEmail,
                    rememberMe = true
                )
            }
        }
    }

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
            errorMessage = null
        )
    }

    fun onRememberMeChange(rememberMe: Boolean) {
        _state.value = _state.value.copy(rememberMe = rememberMe)
    }

    fun togglePasswordVisibility() {
        _state.value = _state.value.copy(
            isPasswordVisible = !_state.value.isPasswordVisible
        )
    }

    fun onLoginClick(onSuccess: () -> Unit) {
        if (!validateInput()) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)

            when (val result = loginUseCase(
                email = _state.value.email.trim(),
                password = _state.value.password,
                rememberMe = _state.value.rememberMe
            )) {
                is AuthResult.Success -> {
                    _state.value = _state.value.copy(isLoading = false)
                    onSuccess()
                }
                is AuthResult.Error -> {
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
        }

        return isValid
    }

    private fun isValidEmail(email: String): Boolean {
        return email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}