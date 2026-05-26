package software.setixx.chimu.api.web

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
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
@Tag(name = "Role Upgrades", description = "User role upgrade request management")
class RoleUpgradeController(
    private val roleUpgradeService: RoleUpgradeService,
    private val userRepository: UserRepository,
    private val roleUpgradeRequestRepository: RoleUpgradeRequestRepository
) {

    @PostMapping("/api/users/me/role-requests")
    @Operation(summary = "Create role upgrade request", description = "Creates a request to upgrade the current user's role")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Role upgrade request created successfully"),
        ApiResponse(responseCode = "400", description = "Invalid request or request already pending")
    )
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
    @Operation(summary = "Get user role upgrade requests", description = "Retrieves all role upgrade requests made by the current user")
    @ApiResponse(responseCode = "200", description = "Role upgrade requests retrieved successfully")
    fun getUserRequests(
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<List<RoleUpgradeRequestResponse>> {
        val user = userRepository.findByPublicIdAndDeletedAtIsNull(userDetails.publicId)
            ?: throw IllegalStateException("User not found")
        return ResponseEntity.ok(roleUpgradeService.getUserRequests(user.id!!))
    }

    @DeleteMapping("/api/users/me/role-requests/{requestId}")
    @Operation(summary = "Cancel role upgrade request", description = "Cancels an active role upgrade request")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Role upgrade request cancelled successfully"),
        ApiResponse(responseCode = "403", description = "Not authorized to cancel this request"),
        ApiResponse(responseCode = "404", description = "Role upgrade request not found")
    )
    fun cancelRequest(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Parameter(description = "Role upgrade request public ID")
        @PathVariable requestId: String
    ): ResponseEntity<RoleUpgradeRequestResponse> {
        val user = userRepository.findByPublicIdAndDeletedAtIsNull(userDetails.publicId)
            ?: throw IllegalStateException("User not found")
        val request = roleUpgradeRequestRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(requestId))
            ?: throw IllegalStateException("Request not found")
        return ResponseEntity.ok(roleUpgradeService.cancelRequest(user.id!!, request.id))
    }

    @GetMapping("/api/admin/role-requests")
    @Operation(summary = "Get all role upgrade requests", description = "Retrieves all user role upgrade requests (Admin only)")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Role upgrade requests retrieved successfully"),
        ApiResponse(responseCode = "403", description = "Not authorized to view role upgrade requests")
    )
    fun getAllRequests(
        @Parameter(description = "Filter requests by status")
        @RequestParam(required = false) status: RoleRequestStatus?
    ): ResponseEntity<List<RoleUpgradeRequestResponse>> {
        return ResponseEntity.ok(roleUpgradeService.getAllRequests(status))
    }

    @PatchMapping("/api/admin/role-requests/{requestId}")
    @Operation(summary = "Review role upgrade request", description = "Approves or rejects a user's role upgrade request (Admin only)")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Role upgrade request reviewed successfully"),
        ApiResponse(responseCode = "400", description = "Invalid review request"),
        ApiResponse(responseCode = "403", description = "Not authorized to review role upgrade requests"),
        ApiResponse(responseCode = "404", description = "Role upgrade request not found")
    )
    fun reviewRequest(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Parameter(description = "Role upgrade request public ID")
        @PathVariable requestId: String,
        @Valid @RequestBody request: ReviewRoleUpgradeRequest
    ): ResponseEntity<RoleUpgradeRequestResponse> {
        val admin = userRepository.findByPublicIdAndDeletedAtIsNull(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        val reviewRequest = roleUpgradeRequestRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(requestId))
            ?: throw IllegalStateException("Request not found")

        return ResponseEntity.ok(roleUpgradeService.reviewRequest(admin.id!!, reviewRequest.id, request))
    }
}