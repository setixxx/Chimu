package software.setixx.chimu.domain.repository

import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.model.CreateRoleUpgrade
import software.setixx.chimu.domain.model.ReviewRoleUpgrade
import software.setixx.chimu.domain.model.RoleUpgrade

interface RoleUpgradeRepository {
    suspend fun getUserRoleUpgrades(): ApiResult<List<RoleUpgrade>>
    suspend fun createRoleUpgrade(data: CreateRoleUpgrade): ApiResult<RoleUpgrade>
    suspend fun reviewRoleUpgrade(requestId: String, data: ReviewRoleUpgrade): ApiResult<RoleUpgrade>
    suspend fun getAllRoleUpgrades(): ApiResult<List<RoleUpgrade>>
    suspend fun cancelRoleUpgrade(requestId: String): ApiResult<RoleUpgrade>
}