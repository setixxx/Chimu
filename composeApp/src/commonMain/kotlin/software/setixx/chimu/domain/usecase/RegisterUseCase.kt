package software.setixx.chimu.domain.usecase

import software.setixx.chimu.domain.model.AuthResult
import software.setixx.chimu.domain.repository.AuthRepository

class RegisterUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): AuthResult<String> {
        return repository.register(email, password)
    }
}