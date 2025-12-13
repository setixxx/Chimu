package setixx.software.Chimu.service

import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Service
import setixx.software.Chimu.domain.User
import setixx.software.Chimu.repository.UserRepository
import setixx.software.Chimu.security.AuthenticationService
import setixx.software.Chimu.dto.ChangePasswordRequest
import setixx.software.Chimu.dto.ChangePasswordResponse
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val authenticationService: AuthenticationService
) {
    fun getUserByPublicId(publicId: UUID) = userRepository.findByPublicId(publicId)

    fun getCurrentUser(email: String): User {
        return userRepository.findByEmail(email)
            ?: throw IllegalStateException("User not found")
    }

    fun changePassword(
        email: String,
        request: ChangePasswordRequest,
        httpRequest: HttpServletRequest? = null
    ): ChangePasswordResponse {
        return authenticationService.changePassword(email, request, httpRequest)
    }
}