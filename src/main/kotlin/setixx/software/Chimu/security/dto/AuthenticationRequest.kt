package setixx.software.Chimu.security.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class AuthenticationRequest(
    @field:NotBlank(message = "Email must not be blank")
    @field:Email(message = "Email must be a valid email address")
    val email: String,
    @field:NotBlank(message = "Password must not be blank")
    val password: String
)