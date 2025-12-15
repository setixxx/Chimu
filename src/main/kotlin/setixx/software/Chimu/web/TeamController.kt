package setixx.software.Chimu.web

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import setixx.software.Chimu.dto.*
import setixx.software.Chimu.security.CustomUserDetails
import setixx.software.Chimu.service.TeamService
import setixx.software.Chimu.repository.UserRepository

@RestController
@RequestMapping("/api/teams")
class TeamController(
    private val teamService: TeamService,
    private val userRepository: UserRepository
) {
    @PostMapping
    fun createTeam(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Valid @RequestBody request: CreateTeamRequest
    ): ResponseEntity<TeamDetailsResponse> {
        val user = userRepository.findByPublicId(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        val team = teamService.createTeam(user.id!!, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(team)
    }

    @GetMapping
    fun getUserTeams(
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<List<TeamResponse>> {
        val user = userRepository.findByPublicId(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        val teams = teamService.getUserTeams(user.id!!)
        return ResponseEntity.ok(teams)
    }

    @GetMapping("/{teamId}")
    fun getTeamDetails(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PathVariable teamId: String
    ): ResponseEntity<TeamDetailsResponse> {
        val user = userRepository.findByPublicId(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        val team = teamService.getTeamDetailsByPublicId(teamId, user.id!!)
        return ResponseEntity.ok(team)
    }

    @PatchMapping("/{teamId}")
    fun updateTeam(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PathVariable teamId: String,
        @Valid @RequestBody request: UpdateTeamRequest
    ): ResponseEntity<TeamDetailsResponse> {
        val user = userRepository.findByPublicId(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        val team = teamService.updateTeamByPublicId(teamId, user.id!!, request)
        return ResponseEntity.ok(team)
    }

    @PostMapping("/join")
    fun joinTeam(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Valid @RequestBody request: JoinTeamRequest
    ): ResponseEntity<TeamDetailsResponse> {
        val user = userRepository.findByPublicId(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        val team = teamService.joinTeamByToken(user.id!!, request.inviteToken)
        return ResponseEntity.ok(team)
    }

    @DeleteMapping("/{teamId}/leave")
    fun leaveTeam(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PathVariable teamId: String
    ): ResponseEntity<Map<String, String>> {
        val user = userRepository.findByPublicId(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        teamService.leaveTeamByPublicId(teamId, user.id!!)
        return ResponseEntity.ok(mapOf("message" to "Successfully left the team"))
    }

    @DeleteMapping("/{teamId}")
    fun deleteTeam(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PathVariable teamId: String
    ): ResponseEntity<Map<String, String>> {
        val user = userRepository.findByPublicId(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        teamService.deleteTeamByPublicId(teamId, user.id!!)
        return ResponseEntity.ok(mapOf("message" to "Team successfully deleted"))
    }

    @PatchMapping("/{teamId}/specialization")
    fun updateMemberSpecialization(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PathVariable teamId: String,
        @Valid @RequestBody request: UpdateMemberSpecializationRequest
    ): ResponseEntity<TeamMemberResponse> {
        val user = userRepository.findByPublicId(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        val member = teamService.updateMemberSpecializationByPublicId(
            teamId,
            user.id!!,
            request.specializationId
        )
        return ResponseEntity.ok(member)
    }

    @PostMapping("/{teamId}/regenerate-token")
    fun regenerateInviteToken(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PathVariable teamId: String
    ): ResponseEntity<Map<String, String>> {
        val user = userRepository.findByPublicId(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        val newToken = teamService.regenerateInviteTokenByPublicId(teamId, user.id!!)
        return ResponseEntity.ok(mapOf("inviteToken" to newToken))
    }
}