package setixx.software.Chimu.security.dto

import jakarta.validation.constraints.NotBlank

data class RefreshTokenRequest(
    @field:NotBlank(message = "Token is required")
    val token: String
)