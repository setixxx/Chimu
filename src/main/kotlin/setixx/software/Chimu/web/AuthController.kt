package setixx.software.Chimu.web

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import setixx.software.Chimu.security.AuthenticationService
import setixx.software.Chimu.security.dto.AuthenticationRequest
import setixx.software.Chimu.security.dto.AuthenticationResponse
import setixx.software.Chimu.security.dto.RefreshTokenRequest
import setixx.software.Chimu.security.dto.TokenResponse

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authenticationService: AuthenticationService
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
}