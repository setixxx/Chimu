package software.setixx.chimu.domain.usecase

import software.setixx.chimu.domain.model.ChangePassword
import software.setixx.chimu.domain.model.ProfileUpdate
import software.setixx.chimu.domain.repository.UserRepository

class ChangePasswordUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(body: ChangePassword) = repository.changePassword(body)
}

class GetCurrentUserUseCase(private val repository: UserRepository) {
    suspend operator fun invoke() = repository.getCurrentUser()
}

class DeleteProfileUseCase(private val repository: UserRepository) {
    suspend operator fun invoke() = repository.deleteProfile()
}

class UpdateProfileUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(request: ProfileUpdate) = repository.updateProfile(request)
}

class GetUserByIdUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(userId: String) = repository.getUserById(userId)
}

