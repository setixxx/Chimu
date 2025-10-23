package setixx.software.Chimu.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import setixx.software.Chimu.model.User
import setixx.software.Chimu.repository.UserRepository
import java.util.UUID

@Service
class UserService {
    @Autowired
    private lateinit var userRepository : UserRepository

    fun getAllUsers() = userRepository.findAll()

    fun getUserByPublicId(publicId: UUID) = userRepository.findByPublicId(publicId)

    fun registerUser(user: User) = userRepository.save(user)
}