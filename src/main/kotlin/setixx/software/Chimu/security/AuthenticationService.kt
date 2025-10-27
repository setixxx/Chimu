package setixx.software.Chimu.security

import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service
import setixx.software.Chimu.security.dto.AuthenticationRequest
import setixx.software.Chimu.security.dto.AuthenticationResponse
import setixx.software.Chimu.service.RefreshTokenService
import java.time.Instant
import java.util.Date

@Service
class AuthenticationService(
    private val authManager: AuthenticationManager,
    private val userDetailsService: UserDetailsService,
    private val tokenService: TokenService,
    private val refreshTokenService: RefreshTokenService,
    @Value("\${JWT_ACCESS_TOKEN_EXPIRATION}") private val accessTokenExpiration: Long = 0,
    @Value("\${JWT_REFRESH_TOKEN_EXPIRATION}") private val refreshTokenExpiration: Long = 0
) {
    fun authentication(
        authenticationRequest: AuthenticationRequest,
        request: HttpServletRequest? = null
    ): AuthenticationResponse {
        authManager.authenticate(
            UsernamePasswordAuthenticationToken(
                authenticationRequest.email,
                authenticationRequest.password
            )
        )

        val user = userDetailsService.loadUserByUsername(authenticationRequest.email)

        val accessToken = createAccessToken(user)
        val refreshToken = createRefreshToken(user)

        val refreshExpiresAt = Instant.now().plusMillis(refreshTokenExpiration)
        refreshTokenService.saveRefreshToken(refreshToken, user, refreshExpiresAt, request)

        return AuthenticationResponse(
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    fun refreshAccessToken(refreshToken: String): String {
        val username = tokenService.extractUsername(refreshToken)

        return username.let { user ->
            val currentUserDetails = userDetailsService.loadUserByUsername(user)
            val refreshTokenUserDetails = refreshTokenService.findUserDetailsByToken(refreshToken)

            if (!tokenService.isTokenValid(refreshToken, currentUserDetails)) {
                throw AuthenticationServiceException("Refresh token expired")
            }

            if (currentUserDetails.username != refreshTokenUserDetails?.username) {
                throw AuthenticationServiceException("Invalid refresh token")
            }

            refreshTokenService.updateLastUsed(refreshToken)

            createAccessToken(currentUserDetails)
        }
    }

    fun logout(refreshToken: String) {
        refreshTokenService.revokeToken(refreshToken)
    }

    private fun createAccessToken(user: UserDetails): String {
        return tokenService.generateToken(
            email = user.username,
            expiration = Date(System.currentTimeMillis() + accessTokenExpiration)
        )
    }

    private fun createRefreshToken(user: UserDetails) = tokenService.generateToken(
        email = user.username,
        expiration = Date(System.currentTimeMillis() + refreshTokenExpiration)
    )
}