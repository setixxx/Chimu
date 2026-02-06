package software.setixx.chimu.domain.repository

import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.model.ProfileUpdate
import software.setixx.chimu.domain.model.User

interface UserRepository {
    suspend fun updateProfile(request: ProfileUpdate): ApiResult<User>
    suspend fun getCurrentUser(): ApiResult<User>
}