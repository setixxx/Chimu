package setixx.software.Chimu.web

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import setixx.software.Chimu.security.AuthenticationService
import setixx.software.Chimu.dto.AuthenticationRequest
import setixx.software.Chimu.dto.AuthenticationResponse
import setixx.software.Chimu.dto.RefreshTokenRequest
import setixx.software.Chimu.dto.RegisterRequest
import setixx.software.Chimu.dto.RegisterResponse
import setixx.software.Chimu.dto.TokenResponse
import setixx.software.Chimu.service.RegistrationService

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authenticationService: AuthenticationService,
    private val registrationService: RegistrationService
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
        @Valid @RequestBody request: RegisterRequest
    ): ResponseEntity<RegisterResponse> {
        val user = registrationService.register(
            request.email.trim(),
            request.password
        )
        val body = RegisterResponse(
            publicId = user.publicId.toString()
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