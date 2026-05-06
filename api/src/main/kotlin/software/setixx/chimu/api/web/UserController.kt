package software.setixx.chimu.api.web

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import software.setixx.chimu.api.dto.ChangePasswordRequest
import software.setixx.chimu.api.dto.ChangePasswordResponse
import software.setixx.chimu.api.dto.PublicUserProfileResponse
import software.setixx.chimu.api.dto.SpecializationResponse
import software.setixx.chimu.api.dto.UpdateProfileRequest
import software.setixx.chimu.api.dto.UserProfileResponse
import software.setixx.chimu.api.repository.UserRepository
import software.setixx.chimu.api.security.CustomUserDetails
import software.setixx.chimu.api.service.SpecializationService
import software.setixx.chimu.api.service.UserService
import java.util.UUID

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User profile management endpoints")
class UserController(
    private val userService: UserService,
    private val specializationService: SpecializationService,
    private val userRepository: UserRepository,
) {
    @GetMapping("/{publicId}")
    @Operation(summary = "Get user by ID", description = "Retrieves user information by public ID")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "User retrieved successfully"),
        ApiResponse(responseCode = "404", description = "User not found")
    )
    fun getUserById(
        @PathVariable publicId: String
    ): ResponseEntity<PublicUserProfileResponse?> {
        val user = userService.getUserByPublicId(UUID.fromString(publicId))
        val body = userService.toPublicUserResponse(user)
        return ResponseEntity.ok(body)
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Retrieves the profile of the currently authenticated user")
    @ApiResponse(responseCode = "200", description = "Profile retrieved successfully")
    fun getCurrentUser(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<UserProfileResponse> {
        println("hello")
        val user = userService.getCurrentUser(userDetails.username)
        return ResponseEntity.ok(userService.toUserResponse(user))
    }

    @PatchMapping("/me")
    @Operation(summary = "Update profile", description = "Updates the profile of the currently authenticated user")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Profile updated successfully"),
        ApiResponse(responseCode = "400", description = "Invalid request data")
    )
    fun updateProfile(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Valid @RequestBody request: UpdateProfileRequest
    ): ResponseEntity<UserProfileResponse> {
        val user = userRepository.findByPublicIdAndDeletedAtIsNull(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        val updatedUser = userService.updateProfile(user.id!!, request)
        return ResponseEntity.ok(userService.toUserResponse(updatedUser))
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password", description = "Changes the password of the currently authenticated user")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Password changed successfully"),
        ApiResponse(responseCode = "400", description = "Invalid old password or new password doesn't meet requirements")
    )
    fun changePassword(
        @AuthenticationPrincipal userDetails: UserDetails,
        @Valid @RequestBody request: ChangePasswordRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ChangePasswordResponse> {
        val response = userService.changePassword(userDetails.username, request, httpRequest)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/me")
    fun deleteMyAccount(
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<Void> {
        val user = userRepository.findByPublicIdAndDeletedAtIsNull(userDetails.publicId)
            ?: throw IllegalArgumentException("User not found")

        userService.softDeleteAccount(user.id!!)

        return ResponseEntity.noContent().build()
    }
}