package software.setixx.chimu.domain.usecase

import software.setixx.chimu.domain.model.AuthResult
import software.setixx.chimu.domain.model.AuthTokens
import software.setixx.chimu.domain.model.User
import software.setixx.chimu.domain.repository.AuthRepository

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(
        email: String,
        password: String,
        rememberMe: Boolean
    ): AuthResult<AuthTokens> {
        return repository.login(email, password, rememberMe)
    }
}

class RegisterUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): AuthResult<String> {
        return repository.register(email, password)
    }
}

class GetCurrentUserUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(): AuthResult<User> {
        return repository.getCurrentUser()
    }
}

class CheckAuthStatusUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(): Boolean {
        return repository.isLoggedIn()
    }
}

class GetSavedEmailUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(): String? {
        return repository.getSavedEmail()
    }
}

class LogoutUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(): AuthResult<Unit> {
        return repository.logout()
    }
}