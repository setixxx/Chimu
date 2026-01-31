package software.setixx.chimu.domain.usecase

import software.setixx.chimu.domain.model.ProfileUpdate
import software.setixx.chimu.domain.model.User
import software.setixx.chimu.domain.repository.ProfileRepository

class UpdateProfileUseCase(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(request: ProfileUpdate): Result<User> {
        return repository.updateProfile(request)
    }
}