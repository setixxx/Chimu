package setixx.software.Chimu.security.dto

data class RegisterRequest(
    val email: String,
    val password: String,
    val displayName: String? = null,
    val role: String? = null
)
