package software.setixx.chimu.api.service

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import software.setixx.chimu.api.domain.User
import software.setixx.chimu.api.domain.UserRole
import software.setixx.chimu.api.exception.EmailAlreadyExistsException
import software.setixx.chimu.api.repository.UserRepository
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