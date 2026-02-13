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
import software.setixx.chimu.api.dto.RegisterTeamRequest
import software.setixx.chimu.api.dto.RegistrationResponse
import software.setixx.chimu.api.dto.UpdateRegistrationStatusRequest
import software.setixx.chimu.api.repository.UserRepository
import software.setixx.chimu.api.security.CustomUserDetails
import software.setixx.chimu.api.service.JamRegistrationService

@RestController
@RequestMapping("/api/jams/{jamId}/registrations")
@Tag(name = "Team Registrations", description = "Team registration management for game jams")
class JamRegistrationController(
    private val registrationService: JamRegistrationService,
    private val userRepository: UserRepository
) {

    @PostMapping
    @Operation(summary = "Register team for jam", description = "Registers a team for a game jam. Only team leaders can register their teams.")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Team registered successfully"),
        ApiResponse(responseCode = "400", description = "Invalid request or registration not allowed"),
        ApiResponse(responseCode = "403", description = "Not authorized to register this team")
    )
    fun registerTeam(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Parameter(description = "Game jam public ID")
        @PathVariable jamId: String,
        @Valid @RequestBody request: RegisterTeamRequest
    ): ResponseEntity<RegistrationResponse> {
        val user = userRepository.findByPublicId(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        val registration = registrationService.registerTeam(user.id!!, jamId, request.teamId)
        return ResponseEntity.status(HttpStatus.CREATED).body(registration)
    }

    @GetMapping
    @Operation(summary = "Get jam registrations", description = "Retrieves all team registrations for a game jam")
    @ApiResponse(responseCode = "200", description = "Registrations retrieved successfully")
    fun getJamRegistrations(
        @Parameter(description = "Game jam public ID")
        @PathVariable jamId: String
    ): ResponseEntity<List<RegistrationResponse>> {
        val registrations = registrationService.getJamRegistrations(jamId)
        return ResponseEntity.ok(registrations)
    }

    @PatchMapping("/{teamId}")
    @Operation(summary = "Update registration status", description = "Updates the status of a team registration. Only organizers can update status.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Registration status updated successfully"),
        ApiResponse(responseCode = "400", description = "Invalid status or request"),
        ApiResponse(responseCode = "403", description = "Not authorized to update registration status"),
        ApiResponse(responseCode = "404", description = "Registration not found")
    )
    fun updateRegistrationStatus(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Parameter(description = "Game jam public ID")
        @PathVariable jamId: String,
        @Parameter(description = "Team public ID")
        @PathVariable teamId: String,
        @Valid @RequestBody request: UpdateRegistrationStatusRequest
    ): ResponseEntity<RegistrationResponse> {
        val user = userRepository.findByPublicId(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        val registration = registrationService.updateRegistrationStatus(
            jamId, teamId, user.id!!, request.status
        )
        return ResponseEntity.ok(registration)
    }

    @DeleteMapping("/{teamId}")
    @Operation(summary = "Withdraw registration", description = "Withdraws a team's registration from a game jam. Only team leaders can withdraw.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Registration withdrawn successfully"),
        ApiResponse(responseCode = "403", description = "Not authorized to withdraw registration"),
        ApiResponse(responseCode = "404", description = "Registration not found")
    )
    fun withdrawRegistration(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Parameter(description = "Game jam public ID")
        @PathVariable jamId: String,
        @Parameter(description = "Team public ID")
        @PathVariable teamId: String
    ): ResponseEntity<Map<String, String>> {
        val user = userRepository.findByPublicId(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        registrationService.withdrawRegistration(jamId, teamId, user.id!!)
        return ResponseEntity.ok(mapOf("message" to "Registration withdrawn successfully"))
    }
}