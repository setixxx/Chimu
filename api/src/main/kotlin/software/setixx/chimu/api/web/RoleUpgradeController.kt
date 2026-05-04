package software.setixx.chimu.api.web

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import software.setixx.chimu.api.domain.RoleRequestStatus
import software.setixx.chimu.api.dto.CreateRoleUpgradeRequest
import software.setixx.chimu.api.dto.ReviewRoleUpgradeRequest
import software.setixx.chimu.api.dto.RoleUpgradeRequestResponse
import software.setixx.chimu.api.repository.RoleUpgradeRequestRepository
import software.setixx.chimu.api.repository.UserRepository
import software.setixx.chimu.api.security.CustomUserDetails
import software.setixx.chimu.api.service.RoleUpgradeService
import java.util.UUID

@RestController
class RoleUpgradeController(
    private val roleUpgradeService: RoleUpgradeService,
    private val userRepository: UserRepository,
    private val roleUpgradeRequestRepository: RoleUpgradeRequestRepository
) {

    @PostMapping("/api/users/me/role-requests")
    fun createRequest(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Valid @RequestBody request: CreateRoleUpgradeRequest
    ): ResponseEntity<RoleUpgradeRequestResponse> {
        val user = userRepository.findByPublicIdAndDeletedAtIsNull(userDetails.publicId)
            ?: throw IllegalStateException("User not found")
        val response = roleUpgradeService.createRequest(user.id!!, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/api/users/me/role-requests")
    fun getUserRequests(
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<List<RoleUpgradeRequestResponse>> {
        val user = userRepository.findByPublicIdAndDeletedAtIsNull(userDetails.publicId)
            ?: throw IllegalStateException("User not found")
        return ResponseEntity.ok(roleUpgradeService.getUserRequests(user.id!!))
    }

    @DeleteMapping("/api/users/me/role-requests/{requestId}")
    fun cancelRequest(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PathVariable requestId: String
    ): ResponseEntity<RoleUpgradeRequestResponse> {
        val user = userRepository.findByPublicIdAndDeletedAtIsNull(userDetails.publicId)
            ?: throw IllegalStateException("User not found")
        val request = roleUpgradeRequestRepository.findByPublicIdAndUserIdAndDeletedAtIsNull(UUID.fromString(requestId), user.id!!)
            ?: throw IllegalStateException("Request not found")
        return ResponseEntity.ok(roleUpgradeService.cancelRequest(user.id!!, request.id))
    }

    @GetMapping("/api/admin/role-requests")
    fun getAllRequests(
        @RequestParam(required = false) status: RoleRequestStatus?
    ): ResponseEntity<List<RoleUpgradeRequestResponse>> {
        return ResponseEntity.ok(roleUpgradeService.getAllRequests(status))
    }

    @PatchMapping("/api/admin/role-requests/{requestId}")
    fun reviewRequest(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PathVariable requestId: String,
        @Valid @RequestBody request: ReviewRoleUpgradeRequest
    ): ResponseEntity<RoleUpgradeRequestResponse> {
        val admin = userRepository.findByPublicIdAndDeletedAtIsNull(userDetails.publicId)
            ?: throw IllegalStateException("User not found")
        val reviewRequest = roleUpgradeRequestRepository.findByPublicIdAndUserIdAndDeletedAtIsNull(UUID.fromString(requestId), admin.id!!)
            ?: throw IllegalStateException("Request not found")
        return ResponseEntity.ok(roleUpgradeService.reviewRequest(admin.id!!, reviewRequest.id, request))
    }
}