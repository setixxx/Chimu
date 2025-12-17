package software.setixx.chimu.domain.usecase

import software.setixx.chimu.domain.repository.AuthRepository

class CheckAuthStatusUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(): Boolean {
        return repository.isLoggedIn()
    }
}