package setixx.software.Chimu.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import setixx.software.Chimu.domain.JamJudge
import setixx.software.Chimu.domain.UserRole
import setixx.software.Chimu.dto.JudgeResponse
import setixx.software.Chimu.repository.*
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
        val jam = gameJamRepository.findByPublicId(UUID.fromString(jamId))
            ?: throw IllegalArgumentException("Game jam not found")

        val organizer = userRepository.findById(organizerId).orElseThrow()

        if (jam.organizerId != organizerId && organizer.role != UserRole.ADMIN) {
            throw IllegalArgumentException("Only the organizer or admin can assign judges")
        }

        val judge = userRepository.findByPublicId(UUID.fromString(judgeUserId))
            ?: throw IllegalArgumentException("Judge user not found")

        if (judge.role != UserRole.JUDGE && judge.role != UserRole.ADMIN) {
            throw IllegalArgumentException("User must have JUDGE or ADMIN role")
        }

        if (jamJudgeRepository.existsByJamIdAndJudgeId(jam.id!!, judge.id!!)) {
            throw IllegalArgumentException("This judge is already assigned to this jam")
        }

        val judgeTeams = teamMemberRepository.findAllByUserId(judge.id!!)
            .map { it.teamId }

        if (judgeTeams.isNotEmpty()) {
            val activeRegistrations = registrationRepository.findAllByJamId(jam.id!!)
                .filter { it.teamId in judgeTeams }

            if (activeRegistrations.isNotEmpty()) {
                throw IllegalArgumentException("Judge cannot be a participant in this jam")
            }
        }

        val jamJudge = JamJudge(
            jamId = jam.id!!,
            judgeId = judge.id!!,
            assignedBy = organizerId
        )

        jamJudgeRepository.save(jamJudge)

        return JudgeResponse(
            userId = judge.publicId.toString(),
            nickname = judge.nickname,
            avatarUrl = judge.avatarUrl,
            assignedAt = jamJudge.assignedAt.toString()
        )
    }

    @Transactional(readOnly = true)
    fun getJamJudges(jamId: String): List<JudgeResponse> {
        val jam = gameJamRepository.findByPublicId(UUID.fromString(jamId))
            ?: throw IllegalArgumentException("Game jam not found")

        val judges = jamJudgeRepository.findAllByJamId(jam.id!!)
        val judgeUsers = userRepository.findAllById(judges.map { it.judgeId })
            .associateBy { it.id }

        return judges.map { jamJudge ->
            val user = judgeUsers[jamJudge.judgeId]!!
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
        val jam = gameJamRepository.findByPublicId(UUID.fromString(jamId))
            ?: throw IllegalArgumentException("Game jam not found")

        val organizer = userRepository.findById(organizerId).orElseThrow()

        if (jam.organizerId != organizerId && organizer.role != UserRole.ADMIN) {
            throw IllegalArgumentException("Only the organizer or admin can remove judges")
        }

        val judge = userRepository.findByPublicId(UUID.fromString(judgeUserId))
            ?: throw IllegalArgumentException("Judge user not found")

        jamJudgeRepository.deleteByJamIdAndJudgeId(jam.id!!, judge.id!!)
    }
}