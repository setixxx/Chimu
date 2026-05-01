package software.setixx.chimu.api.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import software.setixx.chimu.api.domain.GameJam
import software.setixx.chimu.api.domain.GameJamStatus
import software.setixx.chimu.api.domain.JamTeamRegistration
import software.setixx.chimu.api.domain.RegistrationStatus
import software.setixx.chimu.api.domain.Team
import software.setixx.chimu.api.domain.UserRole
import software.setixx.chimu.api.dto.RegistrationResponse
import software.setixx.chimu.api.repository.GameJamRepository
import software.setixx.chimu.api.repository.JamTeamRegistrationRepository
import software.setixx.chimu.api.repository.TeamMemberRepository
import software.setixx.chimu.api.repository.TeamRepository
import software.setixx.chimu.api.repository.UserRepository
import java.time.Instant
import java.util.UUID

@Service
class JamRegistrationService(
    private val registrationRepository: JamTeamRegistrationRepository,
    private val gameJamRepository: GameJamRepository,
    private val teamRepository: TeamRepository,
    private val teamMemberRepository: TeamMemberRepository,
    private val userRepository: UserRepository
) {

    @Transactional
    fun registerTeam(userId: Long, jamId: String, teamId: String): RegistrationResponse {
        val jam = gameJamRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(jamId))
            ?: throw IllegalArgumentException("Game jam not found")

        val team = teamRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(teamId))
            ?: throw IllegalArgumentException("Team not found")

        if (team.leader.id != userId) {
            throw IllegalArgumentException("Only team leader can register the team")
        }

        if (jam.status != GameJamStatus.REGISTRATION_OPEN) {
            throw IllegalArgumentException("Game jam is not open for registration")
        }

        if (registrationRepository.existsByGameJamIdAndTeamIdAndDeletedAtIsNull(jam.id!!, team.id!!)) {
            throw IllegalArgumentException("Team is already registered for this game jam")
        }

        val activeRegistrations = registrationRepository.findActiveRegistrationsByTeamId(team.id!!)
        if (activeRegistrations.isNotEmpty()) {
            val activeJams = gameJamRepository.findAllById(activeRegistrations.map { it.gameJam.id })
            val conflictingJam = activeJams.find { activeJam ->
                isJamActive(activeJam) && (activeJam.id != jam.id)
            }

            if (conflictingJam != null) {
                throw IllegalArgumentException(
                    "Team is already registered for another active jam: ${conflictingJam.name}"
                )
            }
        }

        val teamMembers = teamMemberRepository.findAllByTeamIdAndDeletedAtIsNull(team.id!!)
        val teamSize = teamMembers.size

        if (teamSize < jam.minTeamSize || teamSize > jam.maxTeamSize) {
            throw IllegalArgumentException(
                "Team size ($teamSize) must be between ${jam.minTeamSize} and ${jam.maxTeamSize}"
            )
        }

        val membersWithoutSpecialization = teamMembers.filter { it.specialization?.id == null }
        if (membersWithoutSpecialization.isNotEmpty()) {
            throw IllegalArgumentException(
                "All team members must have a specialization assigned before registration"
            )
        }

        val registration = JamTeamRegistration(
            gameJam = jam,
            team = team,
            status = RegistrationStatus.PENDING,
            registeredBy = userId
        )

        val saved = registrationRepository.save(registration)
        return toRegistrationResponse(saved, jam, team, userId)
    }

    @Transactional(readOnly = true)
    fun getJamRegistrations(jamId: String): List<RegistrationResponse> {
        val jam = gameJamRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(jamId))
            ?: throw IllegalArgumentException("Game jam not found")

        val registrations = registrationRepository.findAllByGameJamIdAndDeletedAtIsNull(jam.id!!)

        return registrations.map { reg ->
            val team = teamRepository.findById(reg.team.id!!).orElseThrow()
            toRegistrationResponse(reg, jam, team, reg.registeredBy)
        }
    }

    @Transactional(readOnly = true)
    fun getTeamRegistrations(teamId: String): List<RegistrationResponse> {
        val team = teamRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(teamId))
            ?: throw IllegalArgumentException("Team not found")

        val registrations = registrationRepository.findAllByTeamIdAndDeletedAtIsNull(team.id!!)

        return registrations.map { reg ->
            val jam = gameJamRepository.findById(reg.gameJam.id!!).orElseThrow()
            toRegistrationResponse(reg, jam, team, reg.registeredBy)
        }
    }

    @Transactional
    fun updateRegistrationStatus(
        jamId: String,
        teamId: String,
        organizerId: Long,
        newStatus: RegistrationStatus
    ): RegistrationResponse {
        val jam = gameJamRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(jamId))
            ?: throw IllegalArgumentException("Game jam not found")

        val team = teamRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(teamId))
            ?: throw IllegalArgumentException("Team not found")

        val organizer = userRepository.findById(organizerId).orElseThrow()
        if (jam.organizer.id != organizerId && organizer.role != UserRole.ADMIN) {
            throw IllegalArgumentException("Only the organizer or admin can update registration status")
        }

        val registration = registrationRepository.findByGameJamIdAndTeamIdAndDeletedAtIsNull(jam.id!!, team.id!!)
            ?: throw IllegalArgumentException("Registration not found")

        if (registration.status == RegistrationStatus.WITHDRAWN) {
            throw IllegalArgumentException("Cannot update withdrawn registration")
        }

        registration.status = newStatus
        registrationRepository.save(registration)

        return toRegistrationResponse(registration, jam, team, registration.registeredBy)
    }

    @Transactional
    fun cancelRegistration(jamId: String, teamId: String, userId: Long): RegistrationResponse {
        val jam = gameJamRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(jamId))
            ?: throw IllegalArgumentException("Game jam not found")

        val team = teamRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(teamId))
            ?: throw IllegalArgumentException("Team not found")

        if (team.leader.id != userId) {
            throw IllegalArgumentException("Only team leader can cancel registration")
        }

        val registration = registrationRepository.findByGameJamIdAndTeamIdAndDeletedAtIsNull(jam.id!!, team.id!!)
            ?: throw IllegalArgumentException("Registration not found")

        if (registration.status == RegistrationStatus.WITHDRAWN) {
            throw IllegalArgumentException("Registration is already withdrawn")
        }

        if (jam.status !in listOf(GameJamStatus.REGISTRATION_OPEN, GameJamStatus.ANNOUNCED) ) {
            throw IllegalArgumentException("Cannot withdraw registration after registration period has ended")
        }

        registrationRepository.softDeleteById(registration.id!!)

        return toRegistrationResponse(registration, jam, team, registration.registeredBy)
    }

    private fun isJamActive(jam: GameJam): Boolean {
        val now = Instant.now()
        return when (jam.status) {
            GameJamStatus.ANNOUNCED,
            GameJamStatus.REGISTRATION_OPEN,
            GameJamStatus.REGISTRATION_CLOSED,
            GameJamStatus.IN_PROGRESS,
            GameJamStatus.JUDGING -> now.isBefore(jam.judgingEnd)
            GameJamStatus.COMPLETED,
            GameJamStatus.CANCELLED -> false
        }
    }

    private fun toRegistrationResponse(
        registration: JamTeamRegistration,
        jam: GameJam,
        team: Team,
        registeredBy: Long
    ): RegistrationResponse {
        val registeredByUser = userRepository.findById(registeredBy).orElseThrow()

        return RegistrationResponse(
            id = registration.id!!,
            jamId = jam.publicId.toString(),
            jamName = jam.name,
            teamId = team.publicId.toString(),
            teamName = team.name,
            status = registration.status,
            registeredAt = registration.registeredAt.toString(),
            registeredBy = registeredByUser.publicId.toString(),
            registeredByNickname = registeredByUser.nickname,
            updatedAt = registration.updatedAt.toString()
        )
    }
}