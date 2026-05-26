package software.setixx.chimu.api.repository

import org.springframework.data.jpa.repository.JpaRepository
import software.setixx.chimu.api.domain.RoleRequestStatus
import software.setixx.chimu.api.domain.RoleUpgradeRequest
import software.setixx.chimu.api.domain.UserRole
import java.util.UUID

interface RoleUpgradeRequestRepository : JpaRepository<RoleUpgradeRequest, Long> {
    fun findAllByStatus(status: RoleRequestStatus): List<RoleUpgradeRequest>
    fun findAllByUserId(userId: Long): List<RoleUpgradeRequest>
    fun existsByUserIdAndRequestedRoleAndStatusAndDeletedAtIsNull(
        userId: Long,
        requestedRole: UserRole,
        status: RoleRequestStatus
    ): Boolean
    fun findByPublicIdAndDeletedAtIsNull(publicId: UUID): RoleUpgradeRequest?
    fun findByIdAndUserIdAndDeletedAtIsNull(id: Long, userId: Long): RoleUpgradeRequest?
}