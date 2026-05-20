package software.setixx.chimu.api.service

import jakarta.persistence.OptimisticLockException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import software.setixx.chimu.api.domain.GameJamStatus
import software.setixx.chimu.api.domain.Team
import software.setixx.chimu.api.domain.TeamMember
import software.setixx.chimu.api.domain.User
import software.setixx.chimu.api.domain.UserRole
import software.setixx.chimu.api.dto.CreateTeamRequest
import software.setixx.chimu.api.dto.SpecializationResponse
import software.setixx.chimu.api.dto.TeamDetailsResponse
import software.setixx.chimu.api.dto.TeamMemberResponse
import software.setixx.chimu.api.dto.TeamResponse
import software.setixx.chimu.api.dto.UpdateTeamRequest
import software.setixx.chimu.api.repository.JamTeamRegistrationRepository
import software.setixx.chimu.api.repository.SpecializationRepository
import software.setixx.chimu.api.repository.TeamMemberRepository
import software.setixx.chimu.api.repository.TeamRepository
import software.setixx.chimu.api.repository.UserRepository
import java.security.SecureRandom
import java.util.*

@Service
class TeamService(
    private val teamRepository: TeamRepository,
    private val teamMemberRepository: TeamMemberRepository,
    private val userRepository: UserRepository,
    private val specializationService: SpecializationService,
    private val registrationRepository: JamTeamRegistrationRepository,
    private val specializationRepository: SpecializationRepository
) {
    private val secureRandom = SecureRandom()

    companion object {
        private val TEAM_JOIN_ALLOWED_JAM_STATUSES = setOf(
            GameJamStatus.COMPLETED,
            GameJamStatus.CANCELLED
        )
    }

    @Transactional
    fun createTeam(userId: Long, request: CreateTeamRequest): TeamDetailsResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }

        if (teamRepository.findByNameAndDeletedAtIsNull(request.name) != null) {
            throw IllegalArgumentException("Team with name '${request.name}' already exists")
        }

        if (user.role != UserRole.PARTICIPANT && user.role != UserRole.ADMIN) {
            throw IllegalArgumentException("Only participant or admin can create a team")
        }

        val userTeamsCount = teamRepository.findAllByLeaderIdAndDeletedAtIsNull(userId).size
        if (userTeamsCount >= 10) {
            throw IllegalArgumentException("You cannot create more than 10 teams")
        }

        val inviteToken = generateInviteToken()

        val team = Team(
            name = request.name,
            description = request.description,
            leader = user,
            inviteToken = inviteToken
        )

        val savedTeam = teamRepository.save(team)

        val teamMember = TeamMember(
            team = savedTeam,
            user = user,
            specialization = user.specialization
        )
        teamMemberRepository.save(teamMember)

        return getTeamDetails(savedTeam.id!!, userId)
    }

    @Transactional(readOnly = true)
    fun getTeamDetails(teamId: Long, requestingUserId: Long): TeamDetailsResponse {
        val team = teamRepository.findById(teamId)
            .orElseThrow { IllegalArgumentException("Team not found") }

        val members = teamMemberRepository.findAllByTeamIdAndDeletedAtIsNull(teamId)
        val userIds = members.map { it.user.id }
        val users = userRepository.findAllById(userIds).associateBy { it.id }

        val memberResponses = members.map { member ->
            val user = users[member.user.id]!!
            toTeamMemberResponse(user, member, team.leader.id!!)
        }

        return TeamDetailsResponse(
            id = team.publicId.toString(),
            name = team.name,
            description = team.description,
            leaderId = userRepository.findById(team.leader.id!!).get().publicId.toString(),
            inviteToken = if (team.leader.id == requestingUserId) team.inviteToken else null,
            createdAt = team.createdAt.toString(),
            members = memberResponses
        )
    }

    @Transactional(readOnly = true)
    fun getTeamDetailsByPublicId(teamPublicId: String, requestingUserId: Long): TeamDetailsResponse {
        val team = teamRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(teamPublicId))
            ?: throw IllegalArgumentException("Team not found")
        return getTeamDetails(team.id!!, requestingUserId)
    }

    @Transactional(readOnly = true)
    fun getUserTeams(userId: Long): List<TeamResponse> {
        val teams = teamRepository.findAllActiveByMemberId(userId)

        return teams.map { team ->
            val memberCount = teamMemberRepository.countByTeamId(team.id!!).toInt()
            TeamResponse(
                id = team.publicId.toString(),
                name = team.name,
                description = team.description,
                leaderId = userRepository.findById(team.leader.id!!).get().publicId.toString(),
                createdAt = team.createdAt.toString(),
                memberCount = memberCount,
                isLeader = team.leader.id == userId
            )
        }
    }

    @Transactional
    fun updateTeam(teamId: Long, userId: Long, request: UpdateTeamRequest): TeamDetailsResponse {
        val team = teamRepository.findById(teamId)
            .orElseThrow { IllegalArgumentException("Team not found") }

        if (team.leader.id != userId) {
            throw IllegalArgumentException("Only team leader can update team information")
        }

        request.name?.let { newName ->
            if (newName != team.name) {
                val existingTeam = teamRepository.findByNameAndDeletedAtIsNull(newName)
                if (existingTeam != null && existingTeam.id != teamId) {
                    throw IllegalArgumentException("Team with name '$newName' already exists")
                }
                team.name = newName
            }
        }
        request.description?.let { team.description = it }

        try {
            teamRepository.save(team)
        } catch (e: OptimisticLockException) {
            throw IllegalStateException("Team was modified by another user. Please refresh and try again.")
        }

        return getTeamDetails(teamId, userId)
    }

    @Transactional
    fun updateTeamByPublicId(teamPublicId: String, userId: Long, request: UpdateTeamRequest): TeamDetailsResponse {
        val team = teamRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(teamPublicId))
            ?: throw IllegalArgumentException("Team not found")
        return updateTeam(team.id!!, userId, request)
    }

    @Transactional
    fun joinTeamByToken(userId: Long, inviteToken: String): TeamDetailsResponse {
        val team = teamRepository.findByInviteTokenAndDeletedAtIsNull(inviteToken)
            ?: throw IllegalArgumentException("Invalid invite token")

        if (teamMemberRepository.existsByTeamIdAndUserIdAndDeletedAtIsNull(team.id!!, userId)) {
            throw IllegalArgumentException("You are already a member of this team")
        }

        val blockingRegistration = registrationRepository.findActiveRegistrationsByTeamId(team.id!!)
            .firstOrNull { it.gameJam.status !in TEAM_JOIN_ALLOWED_JAM_STATUSES }
        if (blockingRegistration != null) {
            val jamName = blockingRegistration.gameJam.name
            throw IllegalArgumentException("Cannot join team while it is registered for an active game jam: $jamName")
        }

        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }

        val teamMember = TeamMember(
            team = team,
            user = user,
            specialization = user.specialization
        )
        teamMemberRepository.save(teamMember)

        return getTeamDetails(team.id!!, userId)
    }

    @Transactional
    fun leaveTeam(teamId: Long, userId: Long) {
        val team = teamRepository.findById(teamId)
            .orElseThrow { IllegalArgumentException("Team not found") }

        if (team.leader.id == userId) {
            throw IllegalArgumentException("Team leader cannot leave the team. Transfer leadership or delete the team.")
        }

        if (!teamMemberRepository.existsByTeamIdAndUserIdAndDeletedAtIsNull(teamId, userId)) {
            throw IllegalArgumentException("You are not a member of this team")
        }

        val activeRegistrations = registrationRepository.findActiveRegistrationsByTeamId(teamId)
        if (activeRegistrations.isNotEmpty()) {
            throw IllegalArgumentException("Cannot leave team while it has active jam registrations. Withdraw from jams first.")
        }

        teamMemberRepository.softDeleteByTeamIdAndUserId(teamId, userId)
    }

    @Transactional
    fun leaveTeamByPublicId(teamPublicId: String, userId: Long) {
        val team = teamRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(teamPublicId))
            ?: throw IllegalArgumentException("Team not found")
        leaveTeam(team.id!!, userId)
    }

    @Transactional
    fun deleteTeam(teamId: Long, userId: Long) {
        val team = teamRepository.findById(teamId)
            .orElseThrow { IllegalArgumentException("Team not found") }

        if (team.leader.id != userId) {
            throw IllegalArgumentException("Only team leader can delete the team")
        }

        val activeRegistrations = registrationRepository.findActiveRegistrationsByTeamId(teamId)
        if (activeRegistrations.isNotEmpty()) {
            throw IllegalArgumentException("Cannot delete team while it has active jam registrations")
        }

        teamRepository.softDeleteById(team.id!!)
    }

    @Transactional
    fun deleteTeamByPublicId(teamPublicId: String, userId: Long) {
        val team = teamRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(teamPublicId))
            ?: throw IllegalArgumentException("Team not found")
        deleteTeam(team.id!!, userId)
    }

    @Transactional
    fun kickMember(teamId: Long, leaderId: Long, memberPublicId: String) {
        val team = teamRepository.findById(teamId)
            .orElseThrow { IllegalArgumentException("Team not found") }

        if (team.leader.id != leaderId) {
            throw IllegalArgumentException("Only team leader can kick members")
        }

        val memberUser = userRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(memberPublicId))
            ?: throw IllegalArgumentException("User not found")

        if (memberUser.id == leaderId) {
            throw IllegalArgumentException("Team leader cannot kick themselves")
        }

        if (!teamMemberRepository.existsByTeamIdAndUserIdAndDeletedAtIsNull(teamId, memberUser.id!!)) {
            throw IllegalArgumentException("User is not a member of this team")
        }

        val activeRegistrations = registrationRepository.findActiveRegistrationsByTeamId(teamId)
        if (activeRegistrations.isNotEmpty()) {
            throw IllegalArgumentException("Cannot kick members while team has active jam registrations")
        }

        teamMemberRepository.softDeleteByTeamIdAndUserId(teamId, memberUser.id!!)
    }

    @Transactional
    fun kickMemberByPublicId(teamPublicId: String, leaderId: Long, memberPublicId: String) {
        val team = teamRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(teamPublicId))
            ?: throw IllegalArgumentException("Team not found")
        kickMember(team.id!!, leaderId, memberPublicId)
    }

    @Transactional
    fun updateMemberSpecialization(
        teamPublicId: String,
        userId: Long,
        specializationId: String
    ): TeamMemberResponse {
        val team = teamRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(teamPublicId))
            ?: throw IllegalArgumentException("Team not found")

        val specialization = specializationService.getSpecializationByPublicId(specializationId)

        val teamMember = teamMemberRepository.findByTeamIdAndUserIdAndDeletedAtIsNull(team.id!!, userId)
            ?: throw IllegalArgumentException("You are not a member of this team")

        teamMember.specialization = specialization
        teamMemberRepository.save(teamMember)

        val userResponse = userRepository.findById(userId).get()
        val teamResponse = teamRepository.findById(team.id!!).get()

        return toTeamMemberResponse(userResponse, teamMember, teamResponse.leader.id!!)
    }

    @Transactional
    fun regenerateInviteToken(teamId: Long, userId: Long): String {
        val team = teamRepository.findById(teamId)
            .orElseThrow { IllegalArgumentException("Team not found") }

        if (team.leader.id != userId) {
            throw IllegalArgumentException("Only team leader can regenerate invite token")
        }

        val newToken = generateInviteToken()
        team.inviteToken = newToken

        try {
            teamRepository.save(team)
        } catch (e: OptimisticLockException) {
            throw IllegalStateException("Team was modified by another user. Please refresh and try again.")
        }

        return newToken
    }

    @Transactional
    fun regenerateInviteTokenByPublicId(teamPublicId: String, userId: Long): String {
        val team = teamRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(teamPublicId))
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
        val specialization = member.specialization?.id?.let {
            val spec = specializationService.getSpecializationById(it)
            SpecializationResponse(spec.publicId.toString(), spec.name, spec.description)
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
