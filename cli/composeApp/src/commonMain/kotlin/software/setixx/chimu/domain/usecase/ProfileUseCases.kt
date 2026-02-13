package software.setixx.chimu.domain.usecase

import software.setixx.chimu.domain.model.ProfileUpdate
import software.setixx.chimu.domain.repository.UserRepository

class UpdateProfileUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(request: ProfileUpdate) = repository.updateProfile(request)
}

class GetCurrentUserUseCase(private val repository: UserRepository) {
    suspend operator fun invoke() = repository.getCurrentUser()
}