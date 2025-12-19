package software.setixx.chimu.api.web

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import software.setixx.chimu.api.dto.AuthenticationRequest
import software.setixx.chimu.api.dto.AuthenticationResponse
import software.setixx.chimu.api.dto.RefreshTokenRequest
import software.setixx.chimu.api.dto.RegisterRequest
import software.setixx.chimu.api.dto.RegisterResponse
import software.setixx.chimu.api.dto.TokenResponse
import software.setixx.chimu.api.security.AuthenticationService
import software.setixx.chimu.api.service.RegistrationService

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