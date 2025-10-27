package setixx.software.Chimu.web

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import setixx.software.Chimu.repository.UserRepository
import setixx.software.Chimu.security.AuthenticationService
import setixx.software.Chimu.security.dto.AuthenticationRequest
import setixx.software.Chimu.security.dto.AuthenticationResponse
import setixx.software.Chimu.security.dto.RefreshTokenRequest
import setixx.software.Chimu.security.dto.RegisterRequest
import setixx.software.Chimu.security.dto.RegisterResponse
import setixx.software.Chimu.security.dto.TokenResponse
import setixx.software.Chimu.service.RegistrationService

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authenticationService: AuthenticationService,
    private val registrationService: RegistrationService,
    private val userRepository: UserRepository
) {
    @PostMapping
    fun authenticate(
        @Valid @RequestBody authRequest: AuthenticationRequest,
        request: HttpServletRequest
    ): ResponseEntity<AuthenticationResponse> {
        val response = authenticationService.authentication(authRequest, request)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/refresh")
    fun refreshAccessToken(
        @Valid @RequestBody request: RefreshTokenRequest
    ): ResponseEntity<TokenResponse> {
        val token = authenticationService.refreshAccessToken(request.token)
        return ResponseEntity.ok(TokenResponse(token = token))
    }

    @PostMapping("/register")
    fun register(
        @Valid @RequestBody req: RegisterRequest
    ): ResponseEntity<RegisterResponse> {
        val user = registrationService.register(
            req.email.trim(),
            req.password,
            req.role
        )
        val body = RegisterResponse(
            publicId = user.publicId.toString(),
            email = user.email,
            role = user.role.name
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(body)
    }

    @PostMapping("/logout")
    fun logout(
        @Valid @RequestBody request: RefreshTokenRequest
    ): ResponseEntity<Map<String, String>> {
        authenticationService.logout(request.token)
        return ResponseEntity.ok(mapOf("message" to "Logged out successfully"))
    }
}