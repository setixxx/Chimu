package software.setixx.chimu.domain.usecase

import software.setixx.chimu.domain.repository.AuthRepository

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(
        email: String,
        password: String,
        rememberMe: Boolean
    ) = repository.login(email, password, rememberMe)
}

class RegisterUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String) =
        repository.register(email, password)
}

class CheckAuthStatusUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke() = repository.isLoggedIn()
}

class GetSavedEmailUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke() = repository.getSavedEmail()
}

class LogoutUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke() = repository.logout()
}