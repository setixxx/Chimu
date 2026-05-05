package software.setixx.chimu.domain.usecase

import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.model.CreateRoleUpgrade
import software.setixx.chimu.domain.model.ReviewRoleUpgrade
import software.setixx.chimu.domain.model.RoleUpgrade
import software.setixx.chimu.domain.repository.RoleUpgradeRepository

class GetUserRoleUpgradesUseCase(
    private val repository: RoleUpgradeRepository
) {
    suspend operator fun invoke(): ApiResult<List<RoleUpgrade>> = repository.getUserRoleUpgrades()
}

class CreateRoleUpgradeUseCase(
    private val repository: RoleUpgradeRepository
) {
    suspend operator fun invoke(data: CreateRoleUpgrade): ApiResult<RoleUpgrade> {
        return repository.createRoleUpgrade(data)
    }
}

class ReviewRoleUpgradeUseCase(
    private val repository: RoleUpgradeRepository
) {
    suspend operator fun invoke(requestId: String, data: ReviewRoleUpgrade): ApiResult<RoleUpgrade> {
        return repository.reviewRoleUpgrade(requestId, data)
    }
}

class GetAllRoleUpgradesUseCase(
    private val repository: RoleUpgradeRepository
) {
    suspend operator fun invoke(): ApiResult<List<RoleUpgrade>> = repository.getAllRoleUpgrades()
}

class CancelRoleUpgradeUseCase(
    private val repository: RoleUpgradeRepository
) {
    suspend operator fun invoke(requestId: String): ApiResult<RoleUpgrade> {
        return repository.cancelRoleUpgrade(requestId)
    }
}