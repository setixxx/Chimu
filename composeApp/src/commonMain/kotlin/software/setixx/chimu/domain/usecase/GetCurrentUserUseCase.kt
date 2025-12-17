package software.setixx.chimu.domain.usecase

import software.setixx.chimu.domain.model.AuthResult
import software.setixx.chimu.domain.model.User
import software.setixx.chimu.domain.repository.AuthRepository

class GetCurrentUserUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(): AuthResult<User> {
        return repository.getCurrentUser()
    }
}