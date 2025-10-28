package setixx.software.Chimu.service

import org.springframework.stereotype.Service
import setixx.software.Chimu.domain.User
import setixx.software.Chimu.repository.UserRepository
import setixx.software.Chimu.security.TokenService
import java.util.UUID

@Service
class UserService(
    private val userRepository : UserRepository

) {
    fun getUserByPublicId(publicId: UUID) = userRepository.findByPublicId(publicId)

    fun getCurrentUser(
        email: String
    ) : User {
        return userRepository.findByEmail(email) ?:
            throw IllegalStateException("User not found")
    }
}