package software.setixx.chimu.domain.usecase

import software.setixx.chimu.domain.model.ProfileUpdateRequest
import software.setixx.chimu.domain.model.User
import software.setixx.chimu.domain.repository.ProfileRepository

class UpdateProfileUseCase(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(request: ProfileUpdateRequest): Result<User> {
        return repository.updateProfile(request)
    }
}