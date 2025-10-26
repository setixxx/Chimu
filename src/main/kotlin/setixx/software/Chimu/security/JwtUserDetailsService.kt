package setixx.software.Chimu.security

import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import setixx.software.Chimu.repository.UserRepository

@Service
class JwtUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {
    override fun loadUserByUsername(email: String): UserDetails {
        val user = userRepository.findByEmail(email)
            ?: throw UsernameNotFoundException("User $email not found!")

        return User.builder()
            .username(user.email)
            .password(user.passwordHash)
            .roles(user.role.name)
            .build()
    }
}