package setixx.software.Chimu.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import setixx.software.Chimu.model.User
import setixx.software.Chimu.repository.UserRepository

@Service
class UserService {
    @Autowired
    private lateinit var userRepository : UserRepository

    fun getAll() : List<User>{
        return userRepository.findAll()
    }
}