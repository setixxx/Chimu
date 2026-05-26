package software.setixx.chimu.domain.usecase

import kotlinx.coroutines.flow.Flow
import software.setixx.chimu.domain.model.ChangePassword
import software.setixx.chimu.domain.model.ProfileUpdate
import software.setixx.chimu.domain.model.PublicUserProfile
import software.setixx.chimu.domain.model.UserProfile
import software.setixx.chimu.domain.repository.UserRepository

class ObserverUserUseCase(
    private val repository: UserRepository
) {
    operator fun invoke(): Flow<UserProfile?> = repository.user
}

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

class GetUserByIdUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(userId: String) =
        repository.getUserById(userId)
}

class GetUserByNicknameUseCase(
    private val repository: UserRepository
){
    suspend operator fun invoke(nickname: String) = repository.getUserByNickname(nickname)
}