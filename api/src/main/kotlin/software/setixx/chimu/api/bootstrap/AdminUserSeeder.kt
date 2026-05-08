package software.setixx.chimu.api.bootstrap

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import software.setixx.chimu.api.domain.User
import software.setixx.chimu.api.domain.UserRole
import software.setixx.chimu.api.repository.UserRepository

@Component
class AdminUserSeeder(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    @Value("\${ADMIN_EMAIL:}") private val adminEmail: String,
    @Value("\${ADMIN_PASSWORD:}") private val adminPassword: String
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        if (adminEmail.isNotBlank() && adminPassword.isNotBlank()) {
            val adminExists = userRepository.findByEmailAndDeletedAtIsNull(adminEmail) != null

            if (!adminExists) {
                val adminUser = User(
                    email = adminEmail,
                    passwordHash = passwordEncoder.encode(adminPassword),
                    role = UserRole.ADMIN,
                    nickname = adminEmail.substringBefore("@"),
                    firstName = "Admin",
                    lastName = "Chimu"
                )
                userRepository.save(adminUser)
                println("Administrator user successfully initialized from environment variables.")
            } else {
                throw IllegalArgumentException("Administrator user with email $adminEmail already exists.")
            }
        } else {
            println("Admin credentials not provided in environment. Skipping admin initialization.")
        }
    }
}