package setixx.software.Chimu.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import setixx.software.Chimu.domain.Team
import setixx.software.Chimu.domain.TeamMember
import setixx.software.Chimu.domain.User
import setixx.software.Chimu.dto.*
import setixx.software.Chimu.repository.TeamMemberRepository
import setixx.software.Chimu.repository.TeamRepository
import setixx.software.Chimu.repository.UserRepository
import java.security.SecureRandom
import java.util.*

@Service
class TeamService(
    private val teamRepository: TeamRepository,
    private val teamMemberRepository: TeamMemberRepository,
    private val userRepository: UserRepository,
    private val specializationService: SpecializationService
) {
    private val secureRandom = SecureRandom()

    @Transactional
    fun createTeam(userId: Long, request: CreateTeamRequest): TeamDetailsResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }

        val inviteToken = generateInviteToken()

        val team = Team(
            name = request.name,
            description = request.description,
            leaderId = userId,
            inviteToken = inviteToken
        )

        val savedTeam = teamRepository.save(team)

        // Автоматически добавляем создателя в команду
        val teamMember = TeamMember(
            teamId = savedTeam.id!!,
            userId = userId,
            specializationId = user.specializationId
        )
        teamMemberRepository.save(teamMember)

        return getTeamDetails(savedTeam.id!!, userId)
    }

    @Transactional(readOnly = true)
    fun getTeamDetails(teamId: Long, requestingUserId: Long): TeamDetailsResponse {
        val team = teamRepository.findById(teamId)
            .orElseThrow { IllegalArgumentException("Team not found") }

        val isMember = teamMemberRepository.existsByTeamIdAndUserId(teamId, requestingUserId)

        val members = teamMemberRepository.findAllByTeamId(teamId)
        val userIds = members.map { it.userId }
        val users = userRepository.findAllById(userIds).associateBy { it.id }

        val memberResponses = members.map { member ->
            val user = users[member.userId]!!
            toTeamMemberResponse(user, member, team.leaderId)
        }

        return TeamDetailsResponse(
            id = team.publicId.toString(),
            name = team.name,
            description = team.description,
            leaderId = userRepository.findById(team.leaderId).get().publicId.toString(),
            inviteToken = if (team.leaderId == requestingUserId) team.inviteToken else null,
            createdAt = team.createdAt.toString(),
            members = memberResponses
        )
    }

    @Transactional(readOnly = true)
    fun getTeamDetailsByPublicId(teamPublicId: String, requestingUserId: Long): TeamDetailsResponse {
        val team = teamRepository.findByPublicId(UUID.fromString(teamPublicId))
            ?: throw IllegalArgumentException("Team not found")
        return getTeamDetails(team.id!!, requestingUserId)
    }

    @Transactional(readOnly = true)
    fun getUserTeams(userId: Long): List<TeamResponse> {
        val teams = teamRepository.findAllByMemberId(userId)

        return teams.map { team ->
            val memberCount = teamMemberRepository.countByTeamId(team.id!!).toInt()
            TeamResponse(
                id = team.publicId.toString(),
                name = team.name,
                description = team.description,
                leaderId = userRepository.findById(team.leaderId).get().publicId.toString(),
                createdAt = team.createdAt.toString(),
                memberCount = memberCount,
                isLeader = team.leaderId == userId
            )
        }
    }

    @Transactional
    fun updateTeam(teamId: Long, userId: Long, request: UpdateTeamRequest): TeamDetailsResponse {
        val team = teamRepository.findById(teamId)
            .orElseThrow { IllegalArgumentException("Team not found") }

        if (team.leaderId != userId) {
            throw IllegalArgumentException("Only team leader can update team information")
        }

        request.name?.let { team.name = it }
        request.description?.let { team.description = it }

        teamRepository.save(team)

        return getTeamDetails(teamId, userId)
    }

    @Transactional
    fun updateTeamByPublicId(teamPublicId: String, userId: Long, request: UpdateTeamRequest): TeamDetailsResponse {
        val team = teamRepository.findByPublicId(UUID.fromString(teamPublicId))
            ?: throw IllegalArgumentException("Team not found")
        return updateTeam(team.id!!, userId, request)
    }

    @Transactional
    fun joinTeamByToken(userId: Long, inviteToken: String): TeamDetailsResponse {
        val team = teamRepository.findByInviteToken(inviteToken)
            ?: throw IllegalArgumentException("Invalid invite token")

        if (teamMemberRepository.existsByTeamIdAndUserId(team.id!!, userId)) {
            throw IllegalArgumentException("You are already a member of this team")
        }

        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }

        val teamMember = TeamMember(
            teamId = team.id!!,
            userId = userId,
            specializationId = user.specializationId
        )
        teamMemberRepository.save(teamMember)

        return getTeamDetails(team.id!!, userId)
    }

    @Transactional
    fun leaveTeam(teamId: Long, userId: Long) {
        val team = teamRepository.findById(teamId)
            .orElseThrow { IllegalArgumentException("Team not found") }

        if (team.leaderId == userId) {
            throw IllegalArgumentException("Team leader cannot leave the team. Transfer leadership or delete the team.")
        }

        if (!teamMemberRepository.existsByTeamIdAndUserId(teamId, userId)) {
            throw IllegalArgumentException("You are not a member of this team")
        }

        teamMemberRepository.deleteByTeamIdAndUserId(teamId, userId)
    }

    @Transactional
    fun leaveTeamByPublicId(teamPublicId: String, userId: Long) {
        val team = teamRepository.findByPublicId(UUID.fromString(teamPublicId))
            ?: throw IllegalArgumentException("Team not found")
        leaveTeam(team.id!!, userId)
    }

    @Transactional
    fun deleteTeam(teamId: Long, userId: Long) {
        val team = teamRepository.findById(teamId)
            .orElseThrow { IllegalArgumentException("Team not found") }

        if (team.leaderId != userId) {
            throw IllegalArgumentException("Only team leader can delete the team")
        }

        teamRepository.delete(team)
    }

    @Transactional
    fun deleteTeamByPublicId(teamPublicId: String, userId: Long) {
        val team = teamRepository.findByPublicId(UUID.fromString(teamPublicId))
            ?: throw IllegalArgumentException("Team not found")
        deleteTeam(team.id!!, userId)
    }

    @Transactional
    fun updateMemberSpecialization(
        teamId: Long,
        userId: Long,
        specializationId: Long?
    ): TeamMemberResponse {
        val teamMember = teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
            ?: throw IllegalArgumentException("You are not a member of this team")

        if (specializationId != null) {
            specializationService.getSpecializationById(specializationId)
        }

        teamMember.specializationId = specializationId
        teamMemberRepository.save(teamMember)

        val user = userRepository.findById(userId).get()
        val team = teamRepository.findById(teamId).get()

        return toTeamMemberResponse(user, teamMember, team.leaderId)
    }

    @Transactional
    fun updateMemberSpecializationByPublicId(
        teamPublicId: String,
        userId: Long,
        specializationId: Long?
    ): TeamMemberResponse {
        val team = teamRepository.findByPublicId(UUID.fromString(teamPublicId))
            ?: throw IllegalArgumentException("Team not found")
        return updateMemberSpecialization(team.id!!, userId, specializationId)
    }

    @Transactional
    fun regenerateInviteToken(teamId: Long, userId: Long): String {
        val team = teamRepository.findById(teamId)
            .orElseThrow { IllegalArgumentException("Team not found") }

        if (team.leaderId != userId) {
            throw IllegalArgumentException("Only team leader can regenerate invite token")
        }

        val newToken = generateInviteToken()
        team.inviteToken = newToken
        teamRepository.save(team)

        return newToken
    }

    @Transactional
    fun regenerateInviteTokenByPublicId(teamPublicId: String, userId: Long): String {
        val team = teamRepository.findByPublicId(UUID.fromString(teamPublicId))
            ?: throw IllegalArgumentException("Team not found")
        return regenerateInviteToken(team.id!!, userId)
    }

    private fun generateInviteToken(): String {
        val bytes = ByteArray(32)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private fun toTeamMemberResponse(
        user: User,
        member: TeamMember,
        leaderId: Long
    ): TeamMemberResponse {
        val specialization = member.specializationId?.let {
            val spec = specializationService.getSpecializationById(it)
            SpecializationResponse(spec.id!!, spec.name, spec.description)
        }

        return TeamMemberResponse(
            userId = user.publicId.toString(),
            nickname = user.nickname,
            avatarUrl = user.avatarUrl,
            specialization = specialization,
            joinedAt = member.joinedAt.toString(),
            isLeader = user.id == leaderId
        )
    }
}