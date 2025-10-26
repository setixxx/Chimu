package setixx.software.Chimu.security.dto

data class AuthenticationRequest(
    val email: String,
    val password: String
)