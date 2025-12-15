package setixx.software.Chimu.service

import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import setixx.software.Chimu.domain.User
import setixx.software.Chimu.dto.*
import setixx.software.Chimu.repository.UserRepository
import setixx.software.Chimu.repository.SkillRepository
import setixx.software.Chimu.security.AuthenticationService
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val skillRepository: SkillRepository,
    private val authenticationService: AuthenticationService
) {
    fun getUserByPublicId(publicId: UUID) = userRepository.findByPublicId(publicId)

    fun getCurrentUser(email: String): User {
        return userRepository.findByEmail(email)
            ?: throw IllegalStateException("User not found")
    }

    @Transactional
    fun updateProfile(userId: Long, request: UpdateProfileRequest): User {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }

        request.firstName?.let { user.firstName = it }
        request.lastName?.let { user.lastName = it }
        request.nickname?.let { newNickname ->
            if (newNickname != user.nickname) {
                val existingUser = userRepository.findByNickname(newNickname)
                if (existingUser != null && existingUser.id != userId) {
                    throw IllegalArgumentException("Nickname already taken")
                }
                user.nickname = newNickname
            }
        }
        request.bio?.let { user.bio = it }
        request.specializationId?.let { user.specializationId = it }
        request.githubUrl?.let { user.githubUrl = it }
        request.telegramUsername?.let { user.telegramUsername = it }
        request.avatarUrl?.let { user.avatarUrl = it }

        request.skillIds?.let { skillIds ->
            user.skills.clear()
            val skills = skillRepository.findAllById(skillIds)
            if (skills.size != skillIds.size) {
                throw IllegalArgumentException("Some skills not found")
            }
            user.skills.addAll(skills)
        }

        return userRepository.save(user)
    }

    fun changePassword(
        email: String,
        request: ChangePasswordRequest,
        httpRequest: HttpServletRequest? = null
    ): ChangePasswordResponse {
        return authenticationService.changePassword(email, request, httpRequest)
    }
}