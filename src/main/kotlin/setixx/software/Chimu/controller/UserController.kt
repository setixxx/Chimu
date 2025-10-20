package setixx.software.Chimu.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import setixx.software.Chimu.model.Users
import setixx.software.Chimu.service.UserService

@RestController
@RequestMapping("/users")
class UserController {
    @Autowired
    private lateinit var userService: UserService

    @GetMapping("")
    fun getUsers() : List<Users>{
        return userService.getAll()
    }
}