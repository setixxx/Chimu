package setixx.software.Chimu.security

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import setixx.software.Chimu.repository.UserRepository
import java.util.UUID

@Service
class JwtUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    override fun loadUserByUsername(email: String): UserDetails {
        val user = userRepository.findByEmail(email)
            ?: throw UsernameNotFoundException("User $email not found!")

        return CustomUserDetails(
            publicId = user.publicId,
            email = user.email,
            passwordHash = user.passwordHash,
            authorities = listOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))
        )
    }

    fun loadUserByPublicId(publicId: UUID): UserDetails {
        val user = userRepository.findByPublicId(publicId)
            ?: throw UsernameNotFoundException("User with publicId $publicId not found!")

        return CustomUserDetails(
            publicId = user.publicId,
            email = user.email,
            passwordHash = user.passwordHash,
            authorities = listOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))
        )
    }
}