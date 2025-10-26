package setixx.software.Chimu.web

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import setixx.software.Chimu.domain.User
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
    private val registrationService: RegistrationService
) {
    @PostMapping
    fun authenticate(
        @RequestBody authRequest: AuthenticationRequest
    ): AuthenticationResponse =
        authenticationService.authentication(authRequest)

    @PostMapping("/refresh")
    fun refreshAccessToken(
        @RequestBody request: RefreshTokenRequest
    ): TokenResponse = TokenResponse(token = authenticationService.refreshAccessToken(request.token))

    @PostMapping("/register")
    fun register(@RequestBody req: RegisterRequest): ResponseEntity<RegisterResponse> {
        val user = registrationService.register(req.email.trim(), req.password, req.role)
        val body = RegisterResponse(
            publicId = user.publicId.toString(),
            email = user.email,
            role = user.role.name
        )
        return ResponseEntity.status(201).body(body)
    }
}
