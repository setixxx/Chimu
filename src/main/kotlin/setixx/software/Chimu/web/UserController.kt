package setixx.software.Chimu.web

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import setixx.software.Chimu.domain.User
import setixx.software.Chimu.service.UserService

@RestController
@RequestMapping("/api/users")
class UserController (
    private val userService: UserService
){
    @GetMapping("")
    fun getUsers() : List<User>{
        return userService.getAllUsers()
    }

    @GetMapping("/{publicId}")
    fun getUserById(@PathVariable publicId: String) : User? {
        return userService.getUserByPublicId(java.util.UUID.fromString(publicId))
    }
}