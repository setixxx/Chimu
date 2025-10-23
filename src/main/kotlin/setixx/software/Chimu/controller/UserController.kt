package setixx.software.Chimu.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import setixx.software.Chimu.model.User
import setixx.software.Chimu.service.UserService

@RestController
@RequestMapping("/users")
class UserController {
    @Autowired
    private lateinit var userService: UserService

    @GetMapping("")
    fun getUsers() : List<User>{
        return userService.getAllUsers()
    }

    @GetMapping("/{publicId}")
    fun getUserById(@PathVariable publicId: String) : User? {
        return userService.getUserByPublicId(java.util.UUID.fromString(publicId))
    }
}