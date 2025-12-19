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
        val jam = gameJamRepository.findByPublicId(UUID.fromString(jamId))
            ?: throw IllegalArgumentException("Game jam not found")

        val team = teamRepository.findByPublicId(UUID.fromString(teamId))
            ?: throw IllegalArgumentException("Team not found")

        if (team.leaderId != userId) {
            throw IllegalArgumentException("Only team leader can register the team")
        }

        if (jam.status != GameJamStatus.ANNOUNCED) {
            throw IllegalArgumentException("Game jam is not open for registration")
        }

        if (registrationRepository.existsByJamIdAndTeamId(jam.id!!, team.id!!)) {
            throw IllegalArgumentException("Team is already registered for this game jam")
        }

        val teamMembers = teamMemberRepository.findAllByTeamId(team.id!!)
        val teamSize = teamMembers.size

        if (teamSize < jam.minTeamSize || teamSize > jam.maxTeamSize) {
            throw IllegalArgumentException(
                "Team size ($teamSize) must be between ${jam.minTeamSize} and ${jam.maxTeamSize}"
            )
        }

        val membersWithoutSpecialization = teamMembers.filter { it.specializationId == null }
        if (membersWithoutSpecialization.isNotEmpty()) {
            throw IllegalArgumentException(
                "All team members must have a specialization assigned before registration"
            )
        }

        val registration = JamTeamRegistration(
            jamId = jam.id!!,
            teamId = team.id!!,
            status = RegistrationStatus.PENDING,
            registeredBy = userId
        )

        val saved = registrationRepository.save(registration)
        return toRegistrationResponse(saved, jam, team, userId)
    }

    @Transactional(readOnly = true)
    fun getJamRegistrations(jamId: String): List<RegistrationResponse> {
        val jam = gameJamRepository.findByPublicId(UUID.fromString(jamId))
            ?: throw IllegalArgumentException("Game jam not found")

        val registrations = registrationRepository.findAllByJamId(jam.id!!)

        return registrations.map { reg ->
            val team = teamRepository.findById(reg.teamId).orElseThrow()
            toRegistrationResponse(reg, jam, team, reg.registeredBy)
        }
    }

    @Transactional(readOnly = true)
    fun getTeamRegistrations(teamId: String): List<RegistrationResponse> {
        val team = teamRepository.findByPublicId(UUID.fromString(teamId))
            ?: throw IllegalArgumentException("Team not found")

        val registrations = registrationRepository.findAllByTeamId(team.id!!)

        return registrations.map { reg ->
            val jam = gameJamRepository.findById(reg.jamId).orElseThrow()
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
        val jam = gameJamRepository.findByPublicId(UUID.fromString(jamId))
            ?: throw IllegalArgumentException("Game jam not found")

        val team = teamRepository.findByPublicId(UUID.fromString(teamId))
            ?: throw IllegalArgumentException("Team not found")

        val organizer = userRepository.findById(organizerId).orElseThrow()
        if (jam.organizerId != organizerId && organizer.role != UserRole.ADMIN) {
            throw IllegalArgumentException("Only the organizer or admin can update registration status")
        }

        val registration = registrationRepository.findByJamIdAndTeamId(jam.id!!, team.id!!)
            ?: throw IllegalArgumentException("Registration not found")

        if (registration.status == RegistrationStatus.WITHDRAWN) {
            throw IllegalArgumentException("Cannot update withdrawn registration")
        }

        registration.status = newStatus
        registrationRepository.save(registration)

        return toRegistrationResponse(registration, jam, team, registration.registeredBy)
    }

    @Transactional
    fun withdrawRegistration(jamId: String, teamId: String, userId: Long): RegistrationResponse {
        val jam = gameJamRepository.findByPublicId(UUID.fromString(jamId))
            ?: throw IllegalArgumentException("Game jam not found")

        val team = teamRepository.findByPublicId(UUID.fromString(teamId))
            ?: throw IllegalArgumentException("Team not found")

        if (team.leaderId != userId) {
            throw IllegalArgumentException("Only team leader can withdraw registration")
        }

        val registration = registrationRepository.findByJamIdAndTeamId(jam.id!!, team.id!!)
            ?: throw IllegalArgumentException("Registration not found")

        if (registration.status == RegistrationStatus.WITHDRAWN) {
            throw IllegalArgumentException("Registration is already withdrawn")
        }

        if (jam.status !in listOf(GameJamStatus.ANNOUNCED, GameJamStatus.DRAFT)) {
            throw IllegalArgumentException("Cannot withdraw registration after jam has started")
        }

        registration.status = RegistrationStatus.WITHDRAWN
        registrationRepository.save(registration)

        return toRegistrationResponse(registration, jam, team, registration.registeredBy)
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