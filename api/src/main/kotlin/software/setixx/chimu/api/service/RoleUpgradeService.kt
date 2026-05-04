package software.setixx.chimu.api.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import software.setixx.chimu.api.domain.RoleRequestStatus
import software.setixx.chimu.api.domain.RoleUpgradeRequest
import software.setixx.chimu.api.domain.UserRole
import software.setixx.chimu.api.dto.CreateRoleUpgradeRequest
import software.setixx.chimu.api.dto.ReviewRoleUpgradeRequest
import software.setixx.chimu.api.dto.RoleUpgradeRequestResponse
import software.setixx.chimu.api.repository.RoleUpgradeRequestRepository
import software.setixx.chimu.api.repository.UserRepository
import java.time.Instant
import java.util.UUID

private val ALLOWED_ROLES = setOf(UserRole.ORGANIZER, UserRole.JUDGE)

@Service
class RoleUpgradeService(
    private val roleUpgradeRequestRepository: RoleUpgradeRequestRepository,
    private val userRepository: UserRepository
) {

    @Transactional
    fun createRequest(userId: Long, request: CreateRoleUpgradeRequest): RoleUpgradeRequestResponse {
        if (request.requestedRole !in ALLOWED_ROLES) {
            throw IllegalArgumentException("Can only request ORGANIZER or JUDGE role")
        }

        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }

        if (user.role == request.requestedRole) {
            throw IllegalArgumentException("User already has the requested role")
        }

        if (roleUpgradeRequestRepository.existsByUserIdAndRequestedRoleAndStatusAndDeletedAtIsNull(
                userId, request.requestedRole, RoleRequestStatus.PENDING
            )
        ) {
            throw IllegalArgumentException("A pending request for this role already exists")
        }

        if (user.role == UserRole.ADMIN) throw IllegalArgumentException("You can't downgrade your role")

        val roleRequest = RoleUpgradeRequest(
            user = user,
            requestedRole = request.requestedRole,
            userMessage = request.userMessage
        )

        val saved = roleUpgradeRequestRepository.save(roleRequest)
        return toResponse(saved)
    }

    @Transactional
    fun cancelRequest(userId: Long, requestId: Long): RoleUpgradeRequestResponse {
        val roleRequest = roleUpgradeRequestRepository.findByIdAndUserIdAndDeletedAtIsNull(requestId, userId)
            ?: throw IllegalArgumentException("Request not found")

        if (roleRequest.status != RoleRequestStatus.PENDING) {
            throw IllegalArgumentException("Only pending requests can be cancelled")
        }

        roleRequest.status = RoleRequestStatus.CANCELLED
        return toResponse(roleUpgradeRequestRepository.save(roleRequest))
    }

    @Transactional(readOnly = true)
    fun getUserRequests(userId: Long): List<RoleUpgradeRequestResponse> {
        return roleUpgradeRequestRepository.findAllByUserId(userId).map { toResponse(it) }
    }

    @Transactional(readOnly = true)
    fun getAllRequests(status: RoleRequestStatus?): List<RoleUpgradeRequestResponse> {
        val requests = if (status != null) {
            roleUpgradeRequestRepository.findAllByStatus(status)
        } else {
            roleUpgradeRequestRepository.findAll()
        }
        return requests.map { toResponse(it) }
    }

    @Transactional
    fun reviewRequest(adminId: Long, requestId: Long, request: ReviewRoleUpgradeRequest): RoleUpgradeRequestResponse {
        if (request.status !in listOf(RoleRequestStatus.APPROVED, RoleRequestStatus.REJECTED)) {
            throw IllegalArgumentException("Status must be APPROVED or REJECTED")
        }

        val admin = userRepository.findById(adminId)
            .orElseThrow { IllegalArgumentException("Admin not found") }

        val roleRequest = roleUpgradeRequestRepository.findById(requestId)
            .orElseThrow { IllegalArgumentException("Request not found") }

        if (roleRequest.status != RoleRequestStatus.PENDING) {
            throw IllegalArgumentException("Only pending requests can be reviewed")
        }

        roleRequest.status = request.status
        roleRequest.adminMessage = request.adminMessage
        roleRequest.reviewedBy = admin
        roleRequest.reviewedAt = Instant.now()

        if (request.status == RoleRequestStatus.APPROVED) {
            val user = userRepository.findById(roleRequest.user.id!!).orElseThrow()
            user.role = roleRequest.requestedRole
            userRepository.save(user)
        }

        return toResponse(roleUpgradeRequestRepository.save(roleRequest))
    }

    private fun toResponse(request: RoleUpgradeRequest): RoleUpgradeRequestResponse {
        return RoleUpgradeRequestResponse(
            id = request.publicId.toString(),
            userId = request.user.publicId.toString(),
            userNickname = request.user.nickname,
            requestedRole = request.requestedRole.name,
            status = request.status,
            userMessage = request.userMessage,
            adminMessage = request.adminMessage,
            reviewedBy = request.reviewedBy?.publicId?.toString(),
            createdAt = request.createdAt.toString(),
            updatedAt = request.updatedAt.toString()
        )
    }
}