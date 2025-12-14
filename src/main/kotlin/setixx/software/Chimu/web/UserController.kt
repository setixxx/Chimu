package setixx.software.Chimu.web

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import setixx.software.Chimu.domain.User
import setixx.software.Chimu.dto.ChangePasswordRequest
import setixx.software.Chimu.dto.ChangePasswordResponse
import setixx.software.Chimu.dto.UserProfileResponse
import setixx.software.Chimu.service.UserService

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
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

        val body = UserProfileResponse(
            id = user.publicId.toString(),
            email = user.email,
            nickname = user.nickname,
            firstName = user.firstName,
            lastName = user.lastName,
            primaryRole = user.primaryRole,
            avatarUrl = user.avatarUrl,
            createdAt = user.createdAt.toString(),
            skills = user.skills.map { it.name }
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