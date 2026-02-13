package software.setixx.chimu.domain.repository

import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.model.ProfileUpdate
import software.setixx.chimu.domain.model.User

interface UserRepository {
    suspend fun updateProfile(request: ProfileUpdate): ApiResult<User>
    suspend fun getCurrentUser(): ApiResult<User>
    suspend fun getUserById(userId: String): ApiResult<User>
    suspend fun changePassword(oldPassword: String, newPassword: String): ApiResult<Unit>
}