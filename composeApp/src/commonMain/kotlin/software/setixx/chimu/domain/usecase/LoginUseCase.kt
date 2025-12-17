package software.setixx.chimu.domain.usecase

import software.setixx.chimu.domain.model.AuthResult
import software.setixx.chimu.domain.model.AuthTokens
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