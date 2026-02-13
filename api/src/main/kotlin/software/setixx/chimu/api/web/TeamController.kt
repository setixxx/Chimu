package software.setixx.chimu.api.web

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import software.setixx.chimu.api.dto.CreateTeamRequest
import software.setixx.chimu.api.dto.TeamDetailsResponse
import software.setixx.chimu.api.dto.TeamMemberResponse
import software.setixx.chimu.api.dto.TeamResponse
import software.setixx.chimu.api.dto.UpdateMemberSpecializationRequest
import software.setixx.chimu.api.dto.UpdateTeamRequest
import software.setixx.chimu.api.repository.UserRepository
import software.setixx.chimu.api.security.CustomUserDetails
import software.setixx.chimu.api.service.TeamService

@RestController
@RequestMapping("/api/teams")
@Tag(name = "Teams", description = "Team management endpoints")
class TeamController(
    private val teamService: TeamService,
    private val userRepository: UserRepository
) {
    @PostMapping
    @Operation(summary = "Create team", description = "Creates a new team. Maximum 10 teams per user.")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Team created successfully"),
        ApiResponse(responseCode = "400", description = "Invalid request or team limit reached")
    )
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
    @Operation(summary = "Get user teams", description = "Retrieves all teams the current user is a member of")
    @ApiResponse(responseCode = "200", description = "Teams retrieved successfully")
    fun getUserTeams(
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<List<TeamResponse>> {
        val user = userRepository.findByPublicId(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        val teams = teamService.getUserTeams(user.id!!)
        return ResponseEntity.ok(teams)
    }

    @GetMapping("/{teamId}")
    @Operation(summary = "Get team details", description = "Retrieves detailed information about a team")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Team details retrieved successfully"),
        ApiResponse(responseCode = "404", description = "Team not found")
    )
    fun getTeamDetails(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Parameter(description = "Team public ID")
        @PathVariable teamId: String
    ): ResponseEntity<TeamDetailsResponse> {
        val user = userRepository.findByPublicId(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        val team = teamService.getTeamDetailsByPublicId(teamId, user.id!!)
        return ResponseEntity.ok(team)
    }

    @PatchMapping("/{teamId}")
    @Operation(summary = "Update team", description = "Updates team information. Only team leaders can update.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Team updated successfully"),
        ApiResponse(responseCode = "403", description = "Not authorized to update this team"),
        ApiResponse(responseCode = "404", description = "Team not found")
    )
    fun updateTeam(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Parameter(description = "Team public ID")
        @PathVariable teamId: String,
        @Valid @RequestBody request: UpdateTeamRequest
    ): ResponseEntity<TeamDetailsResponse> {
        val user = userRepository.findByPublicId(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        val team = teamService.updateTeamByPublicId(teamId, user.id!!, request)
        return ResponseEntity.ok(team)
    }

    @PostMapping("/{inviteToken}")
    @Operation(summary = "Join team", description = "Joins a team using an invite token")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Joined team successfully"),
        ApiResponse(responseCode = "400", description = "Invalid invite token or already a member")
    )
    fun joinTeam(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Parameter(description = "Team invite token")
        @PathVariable inviteToken: String
    ): ResponseEntity<TeamDetailsResponse> {
        val user = userRepository.findByPublicId(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        val team = teamService.joinTeamByToken(user.id!!, inviteToken)
        return ResponseEntity.ok(team)
    }

    @DeleteMapping("/{teamId}/leave")
    @Operation(summary = "Leave team", description = "Leaves a team. Team leaders cannot leave; they must transfer leadership or delete the team. Cannot leave if team has active jam registrations.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Left team successfully"),
        ApiResponse(responseCode = "400", description = "Cannot leave team with active registrations"),
        ApiResponse(responseCode = "403", description = "Team leader cannot leave")
    )
    fun leaveTeam(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Parameter(description = "Team public ID")
        @PathVariable teamId: String
    ): ResponseEntity<Map<String, String>> {
        val user = userRepository.findByPublicId(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        teamService.leaveTeamByPublicId(teamId, user.id!!)
        return ResponseEntity.ok(mapOf("message" to "Successfully left the team"))
    }

    @DeleteMapping("/{teamId}")
    @Operation(summary = "Delete team", description = "Deletes a team. Only team leaders can delete. Cannot delete if team has active jam registrations.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Team deleted successfully"),
        ApiResponse(responseCode = "400", description = "Cannot delete team with active registrations"),
        ApiResponse(responseCode = "403", description = "Not authorized to delete this team")
    )
    fun deleteTeam(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Parameter(description = "Team public ID")
        @PathVariable teamId: String
    ): ResponseEntity<Map<String, String>> {
        val user = userRepository.findByPublicId(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        teamService.deleteTeamByPublicId(teamId, user.id!!)
        return ResponseEntity.ok(mapOf("message" to "Team successfully deleted"))
    }

    @DeleteMapping("/{teamId}/members/{userId}")
    @Operation(summary = "Kick member", description = "Removes a member from the team. Only team leaders can kick members. Cannot kick if team has active registrations.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Member kicked successfully"),
        ApiResponse(responseCode = "400", description = "Cannot kick member with active registrations"),
        ApiResponse(responseCode = "403", description = "Not authorized to kick members")
    )
    fun kickMember(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Parameter(description = "Team public ID")
        @PathVariable teamId: String,
        @Parameter(description = "User public ID to kick")
        @PathVariable userId: String
    ): ResponseEntity<Map<String, String>> {
        val leader = userRepository.findByPublicId(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        teamService.kickMemberByPublicId(teamId, leader.id!!, userId)
        return ResponseEntity.ok(mapOf("message" to "Member successfully kicked from team"))
    }

    @PatchMapping("/{teamId}/specialization")
    @Operation(summary = "Update member specialization", description = "Updates the specialization of the current user in the team")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Specialization updated successfully"),
        ApiResponse(responseCode = "403", description = "Not a member of this team"),
        ApiResponse(responseCode = "404", description = "Team or specialization not found")
    )
    fun updateMemberSpecialization(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Parameter(description = "Team public ID")
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
    @Operation(summary = "Regenerate invite token", description = "Generates a new invite token for the team. Only team leaders can regenerate tokens.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Token regenerated successfully"),
        ApiResponse(responseCode = "403", description = "Not authorized to regenerate token")
    )
    fun regenerateInviteToken(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Parameter(description = "Team public ID")
        @PathVariable teamId: String
    ): ResponseEntity<Map<String, String>> {
        val user = userRepository.findByPublicId(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        val newToken = teamService.regenerateInviteTokenByPublicId(teamId, user.id!!)
        return ResponseEntity.ok(mapOf("inviteToken" to newToken))
    }
}