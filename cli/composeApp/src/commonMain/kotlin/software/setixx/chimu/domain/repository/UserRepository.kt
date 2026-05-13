package software.setixx.chimu.domain.repository

import kotlinx.coroutines.flow.Flow
import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.model.ChangePassword
import software.setixx.chimu.domain.model.ChangedPassword
import software.setixx.chimu.domain.model.ProfileUpdate
import software.setixx.chimu.domain.model.UserProfile

interface UserRepository {
    val user: Flow<UserProfile?>
    suspend fun changePassword(body: ChangePassword): ApiResult<ChangedPassword>
    suspend fun getCurrentUser(): ApiResult<UserProfile>
    suspend fun deleteProfile(): ApiResult<Unit>
    suspend fun updateProfile(request: ProfileUpdate): ApiResult<UserProfile>
    suspend fun getUserById(userId: String): ApiResult<UserProfile>
}