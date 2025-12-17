package software.setixx.chimu.domain.usecase

import software.setixx.chimu.domain.repository.AuthRepository

class GetSavedEmailUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(): String? {
        return repository.getSavedEmail()
    }
}