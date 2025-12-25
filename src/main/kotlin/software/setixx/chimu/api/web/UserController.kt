package software.setixx.chimu.api.web

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.boot.info.GitProperties
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import software.setixx.chimu.api.domain.User
import software.setixx.chimu.api.dto.ChangePasswordRequest
import software.setixx.chimu.api.dto.ChangePasswordResponse
import software.setixx.chimu.api.dto.SpecializationResponse
import software.setixx.chimu.api.dto.UpdateProfileRequest
import software.setixx.chimu.api.dto.UserProfileResponse
import software.setixx.chimu.api.repository.UserRepository
import software.setixx.chimu.api.security.CustomUserDetails
import software.setixx.chimu.api.service.SpecializationService
import software.setixx.chimu.api.service.UserService

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
    fun getUserById(@PathVariable publicId: String): User? {
        return userService.getUserByPublicId(java.util.UUID.fromString(publicId))
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Retrieves the profile of the currently authenticated user")
    @ApiResponse(responseCode = "200", description = "Profile retrieved successfully")
    fun getCurrentUser(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<UserProfileResponse> {
        val user = userService.getCurrentUser(userDetails.username)

        val specialization = user.specializationId?.let { specId ->
            val spec = specializationService.getSpecializationById(specId)
            SpecializationResponse(spec.id!!, spec.name, spec.description)
        }

        val body = UserProfileResponse(
            id = user.publicId.toString(),
            email = user.email,
            nickname = user.nickname,
            firstName = user.firstName,
            lastName = user.lastName,
            specialization = specialization,
            avatarUrl = user.avatarUrl,
            createdAt = user.createdAt.toString(),
            skills = user.skills.map { it.name },
            role = user.role,
            bio = user.bio,
            githubUrl = user.githubUrl,
            telegramUrl = user.telegramUsername
        )
        return ResponseEntity.ok(body)
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
        val user = userRepository.findByPublicId(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        val updatedUser = userService.updateProfile(user.id!!, request)

        val specialization = updatedUser.specializationId?.let { specId ->
            val spec = specializationService.getSpecializationById(specId)
            SpecializationResponse(spec.id!!, spec.name, spec.description)
        }

        val body = UserProfileResponse(
            id = updatedUser.publicId.toString(),
            email = updatedUser.email,
            nickname = updatedUser.nickname,
            firstName = updatedUser.firstName,
            lastName = updatedUser.lastName,
            specialization = specialization,
            avatarUrl = updatedUser.avatarUrl,
            createdAt = updatedUser.createdAt.toString(),
            skills = updatedUser.skills.map { it.name },
            role = user.role,
            bio = user.bio,
            githubUrl = user.githubUrl,
            telegramUrl = user.telegramUsername
        )
        return ResponseEntity.ok(body)
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
}