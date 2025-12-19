package software.setixx.chimu.api.web

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
class JamRegistrationController(
    private val registrationService: JamRegistrationService,
    private val userRepository: UserRepository
) {

    @PostMapping
    fun registerTeam(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PathVariable jamId: String,
        @Valid @RequestBody request: RegisterTeamRequest
    ): ResponseEntity<RegistrationResponse> {
        val user = userRepository.findByPublicId(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        val registration = registrationService.registerTeam(user.id!!, jamId, request.teamId)
        return ResponseEntity.status(HttpStatus.CREATED).body(registration)
    }

    @GetMapping
    fun getJamRegistrations(
        @PathVariable jamId: String
    ): ResponseEntity<List<RegistrationResponse>> {
        val registrations = registrationService.getJamRegistrations(jamId)
        return ResponseEntity.ok(registrations)
    }

    @PatchMapping("/{teamId}")
    fun updateRegistrationStatus(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PathVariable jamId: String,
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
    fun withdrawRegistration(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PathVariable jamId: String,
        @PathVariable teamId: String
    ): ResponseEntity<Map<String, String>> {
        val user = userRepository.findByPublicId(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        registrationService.withdrawRegistration(jamId, teamId, user.id!!)
        return ResponseEntity.ok(mapOf("message" to "Registration withdrawn successfully"))
    }
}