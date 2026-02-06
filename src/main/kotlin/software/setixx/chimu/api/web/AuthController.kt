package software.setixx.chimu.api.web

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
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
import software.setixx.chimu.api.dto.RefreshTokenResponse
import software.setixx.chimu.api.security.AuthenticationService
import software.setixx.chimu.api.service.RegistrationService

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication and registration endpoints")
class AuthController(
    private val authenticationService: AuthenticationService,
    private val registrationService: RegistrationService
) {
    @PostMapping
    @Operation(summary = "Authenticate user", description = "Authenticates a user and returns access and refresh tokens")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Authentication successful"),
        ApiResponse(responseCode = "401", description = "Invalid credentials")
    )
    fun authenticate(
        @Valid @RequestBody authRequest: AuthenticationRequest,
        request: HttpServletRequest
    ): ResponseEntity<AuthenticationResponse> {
        val response = authenticationService.authentication(authRequest, request)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Generates a new access token using a valid refresh token")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
        ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    )
    fun refreshAccessToken(
        @Valid @RequestBody request: RefreshTokenRequest
    ): ResponseEntity<RefreshTokenResponse> {
        val token = authenticationService.refreshAccessToken(request.token)
        return ResponseEntity.ok(RefreshTokenResponse(token = token))
    }

    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Registers a new user account")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "User registered successfully"),
        ApiResponse(responseCode = "400", description = "Invalid request data"),
        ApiResponse(responseCode = "409", description = "Email already exists")
    )
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
    @Operation(summary = "Logout user", description = "Revokes the provided refresh token, logging out the user")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Logged out successfully"),
        ApiResponse(responseCode = "401", description = "Invalid refresh token")
    )
    fun logout(
        @Valid @RequestBody request: RefreshTokenRequest
    ): ResponseEntity<Map<String, String>> {
        authenticationService.logout(request.token)
        return ResponseEntity.ok(mapOf("message" to "Logged out successfully"))
    }
}