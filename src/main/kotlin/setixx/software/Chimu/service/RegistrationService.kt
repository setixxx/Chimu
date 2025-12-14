package setixx.software.Chimu.service

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import setixx.software.Chimu.domain.User
import setixx.software.Chimu.domain.UserRole
import setixx.software.Chimu.exception.EmailAlreadyExistsException
import setixx.software.Chimu.repository.UserRepository
import java.time.Instant

@Service
class RegistrationService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    fun register(email: String, rawPassword: String): User {
        val encodedPassword = passwordEncoder.encode(rawPassword)
        if (userRepository.findByEmail(email) != null) {
            throw EmailAlreadyExistsException("User with email $email already exists")
        }

        val user = User(
            email = email,
            passwordHash = encodedPassword,
            role = UserRole.PARTICIPANT,
            nickname = email.substringBefore("@"),
            createdAt = Instant.now()
        )

        return userRepository.save(user)
    }
}