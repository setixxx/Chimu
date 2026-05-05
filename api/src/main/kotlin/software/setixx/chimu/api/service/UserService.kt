package software.setixx.chimu.api.service

import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import software.setixx.chimu.api.domain.GameJamStatus
import software.setixx.chimu.api.domain.User
import software.setixx.chimu.api.domain.UserSkill
import software.setixx.chimu.api.dto.ChangePasswordRequest
import software.setixx.chimu.api.dto.ChangePasswordResponse
import software.setixx.chimu.api.dto.PublicUserProfileResponse
import software.setixx.chimu.api.dto.SkillResponse
import software.setixx.chimu.api.dto.SpecializationResponse
import software.setixx.chimu.api.dto.UpdateProfileRequest
import software.setixx.chimu.api.dto.UserProfileResponse
import software.setixx.chimu.api.repository.GameJamRepository
import software.setixx.chimu.api.repository.JamJudgeRepository
import software.setixx.chimu.api.repository.SkillRepository
import software.setixx.chimu.api.repository.TeamRepository
import software.setixx.chimu.api.repository.UserRepository
import software.setixx.chimu.api.security.AuthenticationService
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val teamRepository: TeamRepository,
    private val gameJamRepository: GameJamRepository,
    private val jamJudgeRepository: JamJudgeRepository,
    private val skillRepository: SkillRepository,
    private val authenticationService: AuthenticationService,
    private val specializationService: SpecializationService

) {
    fun getUserByPublicId(publicId: UUID): User {
        return userRepository.findByPublicIdAndDeletedAtIsNull(publicId)
            ?: throw IllegalStateException("User not found")
    }

    fun getCurrentUser(email: String): User {
        return userRepository.findByEmailAndDeletedAtIsNull(email)
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
                val existingUser = userRepository.findByNicknameAndDeletedAtIsNull(newNickname)
                if (existingUser != null && existingUser.id != userId) {
                    throw IllegalArgumentException("Nickname already taken")
                }
                user.nickname = newNickname
            }
        }
        request.bio?.let { user.bio = it }

        if (request.specializationId != null) {
            user.specialization = specializationService.getSpecializationById(request.specializationId)
        } else {
            user.specialization = null
        }

        request.telegramUsername?.let { user.telegramUsername = it }
        request.avatarUrl?.let { user.avatarUrl = it }

        request.skillIds?.let { skillIds ->
            user.skills.clear()
            val skills = skillRepository.findAllById(skillIds)
            if (skills.size != skillIds.size) {
                throw IllegalArgumentException("Some skills not found")
            }

            val userSkills = skills.map { skill ->
                UserSkill(user = user, skill = skill)
            }

            user.skills.addAll(userSkills)
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

    @Transactional
    fun softDeleteAccount(userId: Long) {
        if (teamRepository.existsByLeaderIdAndDeletedAtIsNull(userId)) {
            throw IllegalStateException("Cannot delete account: transfer leadership first.")
        }

        val excludedJamStatuses = listOf(GameJamStatus.COMPLETED, GameJamStatus.CANCELLED)
        if (gameJamRepository.existsByOrganizerIdAndDeletedAtIsNullAndStatusNotIn(userId, excludedJamStatuses)) {
            throw IllegalStateException("Cannot delete account: transfer game jam ownership first.")
        }

        if (jamJudgeRepository.isJudgeInOngoingJam(userId)) {
            throw IllegalStateException("Cannot delete account: active judge in an ongoing jam.")
        }

        userRepository.softDeleteById(userId)
    }

    fun toPublicUserResponse(user: User): PublicUserProfileResponse {
        if (user.deletedAt != null) {
            return PublicUserProfileResponse(
                id = user.publicId.toString(),
                nickname = user.nickname,
                isDeleted = true
            )
        }

        val specialization = user.specialization?.let { spec ->
            SpecializationResponse(spec.id!!, spec.name, spec.description)
        }

        return PublicUserProfileResponse(
            id = user.publicId.toString(),
            nickname = user.nickname,
            isDeleted = false,
            firstName = user.firstName,
            lastName = user.lastName,
            specialization = specialization,
            avatarUrl = user.avatarUrl,
            createdAt = user.createdAt.toString(),
            skills = user.skills.map { SkillResponse(id = it.skill.id!!, name = it.skill.name) },
            bio = user.bio,
            githubUrl = user.githubUrl,
            telegramUrl = user.telegramUsername,
        )
    }

    fun toUserResponse(user: User): UserProfileResponse {
        val specialization = user.specialization?.let { spec ->
            SpecializationResponse(spec.id!!, spec.name, spec.description)
        }

        val skills = user.skills.map { userSkill ->
            SkillResponse(id = userSkill.skill.id!!, name = userSkill.skill.name)
        }

        return UserProfileResponse(
            id = user.publicId.toString(),
            email = user.email,
            nickname = user.nickname,
            firstName = user.firstName,
            lastName = user.lastName,
            specialization = specialization,
            avatarUrl = user.avatarUrl,
            createdAt = user.createdAt.toString(),
            skills = skills,
            role = user.role,
            bio = user.bio,
            githubUrl = user.githubUrl,
            telegramUrl = user.telegramUsername
        )
    }
}