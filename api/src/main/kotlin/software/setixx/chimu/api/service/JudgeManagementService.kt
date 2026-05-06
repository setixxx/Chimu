package software.setixx.chimu.api.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import software.setixx.chimu.api.domain.GameJamStatus
import software.setixx.chimu.api.domain.JamJudge
import software.setixx.chimu.api.domain.UserRole
import software.setixx.chimu.api.dto.JudgeResponse
import software.setixx.chimu.api.repository.GameJamRepository
import software.setixx.chimu.api.repository.JamJudgeRepository
import software.setixx.chimu.api.repository.JamTeamRegistrationRepository
import software.setixx.chimu.api.repository.TeamMemberRepository
import software.setixx.chimu.api.repository.UserRepository
import java.util.UUID

@Service
class JudgeManagementService(
    private val jamJudgeRepository: JamJudgeRepository,
    private val gameJamRepository: GameJamRepository,
    private val userRepository: UserRepository,
    private val teamMemberRepository: TeamMemberRepository,
    private val registrationRepository: JamTeamRegistrationRepository
) {

    @Transactional
    fun assignJudge(jamId: String, organizerId: Long, judgeUserId: String): JudgeResponse {
        val jam = gameJamRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(jamId))
            ?: throw IllegalArgumentException("Game jam not found")

        if (jam.status !in listOf(
                GameJamStatus.DRAFT,
                GameJamStatus.ANNOUNCED,
                GameJamStatus.REGISTRATION_OPEN,
                GameJamStatus.REGISTRATION_CLOSED
            )
        ) {
            throw IllegalArgumentException("Cannot assign judge after jam started")
        }

        val organizer = userRepository.findById(organizerId).orElseThrow()

        if (jam.organizer.id != organizerId && organizer.role != UserRole.ADMIN) {
            throw IllegalArgumentException("Only the organizer or admin can assign judges")
        }

        val judge = userRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(judgeUserId))
            ?: throw IllegalArgumentException("Judge user not found")

        if (judge.role != UserRole.JUDGE && judge.role != UserRole.ADMIN) {
            throw IllegalArgumentException("User must have JUDGE or ADMIN role")
        }

        if (judge.id == jam.organizer.id) {
            throw IllegalArgumentException("Organizer cannot be a judge for their own jam")
        }

        if (jamJudgeRepository.existsByGameJamIdAndJudgeIdAndDeletedAtIsNull(jam.id!!, judge.id!!)) {
            throw IllegalArgumentException("This judge is already assigned to this jam")
        }

        val judgeTeams = teamMemberRepository.findAllByUserIdAndDeletedAtIsNull(judge.id!!)
            .map { it.team.id }

        if (judgeTeams.isNotEmpty()) {
            val activeRegistrations = registrationRepository.findAllByGameJamIdAndDeletedAtIsNull(jam.id!!)
                .filter { it.team.id in judgeTeams }

            if (activeRegistrations.isNotEmpty()) {
                throw IllegalArgumentException("Judge cannot be a participant in this jam")
            }
        }

        val jamJudge = JamJudge(
            gameJam = jam,
            judge = judge,
            assignedBy = organizer
        )

        val savedJudge = jamJudgeRepository.save(jamJudge)

        return JudgeResponse(
            userId = judge.publicId.toString(),
            nickname = judge.nickname,
            avatarUrl = judge.avatarUrl,
            assignedAt = savedJudge.assignedAt.toString()
        )
    }

    @Transactional(readOnly = true)
    fun getJamJudges(jamId: String): List<JudgeResponse> {
        val jam = gameJamRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(jamId))
            ?: throw IllegalArgumentException("Game jam not found")

        val judges = jamJudgeRepository.findAllByGameJamIdAndDeletedAtIsNull(jam.id!!)
        val judgeUsers = userRepository.findAllById(judges.map { it.judge.id })
            .associateBy { it.id }

        return judges.map { jamJudge ->
            val user = judgeUsers[jamJudge.judge.id]!!
            JudgeResponse(
                userId = user.publicId.toString(),
                nickname = user.nickname,
                avatarUrl = user.avatarUrl,
                assignedAt = jamJudge.assignedAt.toString()
            )
        }
    }

    @Transactional
    fun removeJudge(jamId: String, organizerId: Long, judgeUserId: String) {
        val jam = gameJamRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(jamId))
            ?: throw IllegalArgumentException("Game jam not found")

        val organizer = userRepository.findById(organizerId).orElseThrow()

        if (jam.organizer.id != organizerId && organizer.role != UserRole.ADMIN) {
            throw IllegalArgumentException("Only the organizer or admin can remove judges")
        }

        if (jam.status == GameJamStatus.COMPLETED) {
            throw IllegalStateException("It is not possible to remove a judge from a completed jam.")
        }

        val judge = userRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(judgeUserId))
            ?: throw IllegalArgumentException("Judge user not found")

        jamJudgeRepository.softDeleteByJamIdAndJudgeId(jam.id!!, judge.id!!)
    }
}