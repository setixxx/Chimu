package setixx.software.Chimu.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service
import setixx.software.Chimu.repository.RefreshTokenRepository
import setixx.software.Chimu.security.dto.AuthenticationRequest
import setixx.software.Chimu.security.dto.AuthenticationResponse
import java.util.Date

@Service
class AuthenticationService(
    private val authManager: AuthenticationManager,
    private val userDetailsService: UserDetailsService,
    private val tokenService: TokenService,
    private val refreshTokenRepository: RefreshTokenRepository,
    @Value("\${jwt.accessTokenExpiration}") private val accessTokenExpiration: Long = 0,
    @Value("\${jwt.refreshTokenExpiration}") private val refreshTokenExpiration: Long = 0
) {
    fun authentication(authenticationRequest: AuthenticationRequest): AuthenticationResponse {
        authManager.authenticate(
            UsernamePasswordAuthenticationToken(
                authenticationRequest.email,
                authenticationRequest.password
            )
        )

        val user = userDetailsService.loadUserByUsername(authenticationRequest.email)

        val accessToken = createAccessToken(user)
        val refreshToken = createRefreshToken(user)

        refreshTokenRepository.save(refreshToken, user)

        return AuthenticationResponse(
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    fun refreshAccessToken(refreshToken: String): String {
        val username = tokenService.extractUsername(refreshToken)

        return username.let { user ->
            val currentUserDetails = userDetailsService.loadUserByUsername(user)
            val refreshTokenUserDetails = refreshTokenRepository.findUserDetailsByToken(refreshToken)

            if (currentUserDetails.username == refreshTokenUserDetails?.username)
                createAccessToken(currentUserDetails)
            else
                throw AuthenticationServiceException("Invalid refresh token")
        }
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