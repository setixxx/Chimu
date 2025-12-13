package setixx.software.Chimu.security

import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import setixx.software.Chimu.repository.UserRepository
import setixx.software.Chimu.dto.AuthenticationRequest
import setixx.software.Chimu.dto.AuthenticationResponse
import setixx.software.Chimu.dto.ChangePasswordRequest
import setixx.software.Chimu.dto.ChangePasswordResponse
import setixx.software.Chimu.service.RefreshTokenService
import java.time.Instant
import java.util.Date

@Service
class AuthenticationService(
    private val authManager: AuthenticationManager,
    private val userDetailsService: UserDetailsService,
    private val tokenService: TokenService,
    private val refreshTokenService: RefreshTokenService,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
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
        val username = tokenService.extractEmail(refreshToken)

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
        val tokenType = tokenService.extractTokenType(refreshToken)

        if (tokenType != "refresh") {
            throw AuthenticationServiceException("Invalid token type. Expected a refresh token")
        }

        refreshTokenService.revokeToken(refreshToken)
    }

    fun changePassword(
        userEmail: String,
        request: ChangePasswordRequest,
        httpRequest: HttpServletRequest? = null
    ): ChangePasswordResponse {
        val user = userRepository.findByEmail(userEmail)
            ?: throw IllegalStateException("User not found")

        if (!passwordEncoder.matches(request.oldPassword, user.passwordHash)) {
            throw IllegalArgumentException("Old password is incorrect")
        }

        user.passwordHash = passwordEncoder.encode(request.newPassword)
        userRepository.save(user)

        refreshTokenService.revokeAllUserTokens(user.id!!)

        val userDetails = userDetailsService.loadUserByUsername(userEmail)
        val newAccessToken = createAccessToken(userDetails)
        val newRefreshToken = createRefreshToken(userDetails)

        val refreshExpiresAt = Instant.now().plusMillis(refreshTokenExpiration)
        refreshTokenService.saveRefreshToken(newRefreshToken, userDetails, refreshExpiresAt, httpRequest)

        return ChangePasswordResponse(
            message = "Password changed successfully",
            accessToken = newAccessToken,
            refreshToken = newRefreshToken
        )
    }

    private fun createAccessToken(user: UserDetails): String {
        return tokenService.generateToken(
            email = user.username,
            expiration = Date(System.currentTimeMillis() + accessTokenExpiration),
            tokenType = "access"
        )
    }

    private fun createRefreshToken(user: UserDetails): String {
        return tokenService.generateToken(
            email = user.username,
            expiration = Date(System.currentTimeMillis() + refreshTokenExpiration),
            tokenType = "refresh"
        )
    }
}