package software.setixx.chimu.domain.repository

import software.setixx.chimu.domain.model.ProfileUpdate
import software.setixx.chimu.domain.model.User

interface ProfileRepository {
    suspend fun updateProfile(request: ProfileUpdate): Result<User>
}