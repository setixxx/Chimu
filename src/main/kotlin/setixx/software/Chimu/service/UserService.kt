package setixx.software.Chimu.service

import org.springframework.stereotype.Service
import setixx.software.Chimu.repository.UserRepository
import java.util.UUID

@Service
class UserService(
    private val userRepository : UserRepository
) {
    fun getAllUsers() = userRepository.findAll()

    fun getUserByPublicId(publicId: UUID) = userRepository.findByPublicId(publicId)
}