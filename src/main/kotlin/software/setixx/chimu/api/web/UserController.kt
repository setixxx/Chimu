package software.setixx.chimu.api.web

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
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
class UserController(
    private val userService: UserService,
    private val specializationService: SpecializationService,
    private val userRepository: UserRepository
) {
    @GetMapping("/{publicId}")
    fun getUserById(@PathVariable publicId: String): User? {
        return userService.getUserByPublicId(java.util.UUID.fromString(publicId))
    }

    @GetMapping("/me")
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
            skills = user.skills.map { it.name }
        )
        return ResponseEntity.ok(body)
    }

    @PatchMapping("/me")
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
            skills = updatedUser.skills.map { it.name }
        )
        return ResponseEntity.ok(body)
    }

    @PostMapping("/change-password")
    fun changePassword(
        @AuthenticationPrincipal userDetails: UserDetails,
        @Valid @RequestBody request: ChangePasswordRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ChangePasswordResponse> {
        val response = userService.changePassword(userDetails.username, request, httpRequest)
        return ResponseEntity.ok(response)
    }
}