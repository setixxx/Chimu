package software.setixx.chimu.domain.repository

import software.setixx.chimu.domain.model.ProfileUpdateRequest
import software.setixx.chimu.domain.model.User

interface ProfileRepository {
    suspend fun updateProfile(request: ProfileUpdateRequest): Result<User>
}