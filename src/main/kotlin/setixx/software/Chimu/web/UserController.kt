package setixx.software.Chimu.web

import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import setixx.software.Chimu.domain.User
import setixx.software.Chimu.security.dto.AccessTokenRequest
import setixx.software.Chimu.security.dto.UserProfileResponse
import setixx.software.Chimu.service.UserService

@RestController
@RequestMapping("/api/users")
class UserController (
    private val userService: UserService
){
    @GetMapping("/{publicId}")
    fun getUserById(@PathVariable publicId: String) : User? {
        return userService.getUserByPublicId(java.util.UUID.fromString(publicId))
    }

    @GetMapping("/me")
    fun getCurrentUser(
        @AuthenticationPrincipal userDetails: UserDetails
    ) : ResponseEntity<UserProfileResponse> {
        val user = userService.getCurrentUser(userDetails.username)
        val body = UserProfileResponse(
            displayName = user.displayName,
            email = user.email,
            avatarUrl = user.avatarUrl,
            createdAt = user.createdAt.toString()
        )
        return ResponseEntity.ok(body)
    }
}