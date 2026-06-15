package software.setixx.chimu.api.web

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import software.setixx.chimu.api.domain.UserRole
import software.setixx.chimu.api.dto.ForceStatusRequest
import software.setixx.chimu.api.dto.GameJamDetailsResponse
import software.setixx.chimu.api.repository.UserRepository
import software.setixx.chimu.api.security.CustomUserDetails
import software.setixx.chimu.api.service.AdminJamService

@RestController
@RequestMapping("/api/admin/jams")
@Tag(name = "Admin", description = "Admin-only jam management")
class AdminJamController(
    private val adminJamService: AdminJamService,
    private val userRepository: UserRepository
) {

    @PostMapping("/{jamId}/force-status")
    @Operation(summary = "Force jam status transition (admin only)")
    fun forceStatus(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PathVariable jamId: String,
        @Valid @RequestBody request: ForceStatusRequest
    ): ResponseEntity<GameJamDetailsResponse> {
        val user = userRepository.findByPublicIdAndDeletedAtIsNull(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        if (user.role != UserRole.ADMIN) {
            throw IllegalAccessException("Admin only")
        }

        val jam = adminJamService.forceStatus(jamId, user, request.status)
        return ResponseEntity.ok(jam)
    }
}

