package software.setixx.chimu.domain.usecase

import software.setixx.chimu.domain.model.AuthResult
import software.setixx.chimu.domain.repository.AuthRepository

class LogoutUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(): AuthResult<Unit> {
        return repository.logout()
    }
}