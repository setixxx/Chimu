package setixx.software.Chimu.service

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import setixx.software.Chimu.domain.User
import setixx.software.Chimu.domain.UserRole
import setixx.software.Chimu.repository.UserRepository

@Service
class RegistrationService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    fun register(email: String, rawPassword: String, roleStr: String?): User{
        val encodedPassword = passwordEncoder.encode(rawPassword)
        val role = try {
            roleStr?.let { UserRole.valueOf(it.uppercase()) } ?: UserRole.PARTICIPANT
        } catch (ex: IllegalArgumentException) {
            UserRole.PARTICIPANT
        }
        val user = User(
            email = email,
            passwordHash = encodedPassword,
            role = role,
            displayName = email.substringBefore("@"),
        )

        return userRepository.save(user)
    }
}