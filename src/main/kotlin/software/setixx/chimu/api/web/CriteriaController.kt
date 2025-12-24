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
import software.setixx.chimu.api.dto.CreateCriteriaRequest
import software.setixx.chimu.api.dto.CriteriaResponse
import software.setixx.chimu.api.dto.UpdateCriteriaRequest
import software.setixx.chimu.api.repository.UserRepository
import software.setixx.chimu.api.security.CustomUserDetails
import software.setixx.chimu.api.service.CriteriaService

@RestController
@RequestMapping("/api/jams/{jamId}/criteria")
@Tag(name = "Rating Criteria", description = "Rating criteria management for game jams")
class CriteriaController(
    private val criteriaService: CriteriaService,
    private val userRepository: UserRepository
) {

    @PostMapping
    @Operation(summary = "Create rating criteria", description = "Adds a new rating criterion to a game jam")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Criteria created successfully"),
        ApiResponse(responseCode = "400", description = "Invalid request data"),
        ApiResponse(responseCode = "403", description = "Not authorized to add criteria")
    )
    fun createCriteria(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Parameter(description = "Game jam public ID")
        @PathVariable jamId: String,
        @Valid @RequestBody request: CreateCriteriaRequest
    ): ResponseEntity<CriteriaResponse> {
        val user = userRepository.findByPublicId(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        val criteria = criteriaService.createCriteria(jamId, user.id!!, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(criteria)
    }

    @GetMapping
    @Operation(summary = "Get jam criteria", description = "Retrieves all rating criteria for a game jam")
    @ApiResponse(responseCode = "200", description = "Criteria retrieved successfully")
    fun getJamCriteria(
        @Parameter(description = "Game jam public ID")
        @PathVariable jamId: String
    ): ResponseEntity<List<CriteriaResponse>> {
        val criteria = criteriaService.getJamCriteria(jamId)
        return ResponseEntity.ok(criteria)
    }

    @PatchMapping("/{criteriaId}")
    @Operation(summary = "Update criteria", description = "Updates a rating criterion")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Criteria updated successfully"),
        ApiResponse(responseCode = "403", description = "Not authorized to update criteria"),
        ApiResponse(responseCode = "404", description = "Criteria not found")
    )
    fun updateCriteria(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Parameter(description = "Game jam public ID")
        @PathVariable jamId: String,
        @Parameter(description = "Criteria ID")
        @PathVariable criteriaId: Long,
        @Valid @RequestBody request: UpdateCriteriaRequest
    ): ResponseEntity<CriteriaResponse> {
        val user = userRepository.findByPublicId(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        val criteria = criteriaService.updateCriteria(jamId, criteriaId, user.id!!, request)
        return ResponseEntity.ok(criteria)
    }

    @DeleteMapping("/{criteriaId}")
    @Operation(summary = "Delete criteria", description = "Deletes a rating criterion")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Criteria deleted successfully"),
        ApiResponse(responseCode = "403", description = "Not authorized to delete criteria"),
        ApiResponse(responseCode = "404", description = "Criteria not found")
    )
    fun deleteCriteria(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Parameter(description = "Game jam public ID")
        @PathVariable jamId: String,
        @Parameter(description = "Criteria ID")
        @PathVariable criteriaId: Long
    ): ResponseEntity<Map<String, String>> {
        val user = userRepository.findByPublicId(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        criteriaService.deleteCriteria(jamId, criteriaId, user.id!!)
        return ResponseEntity.ok(mapOf("message" to "Criteria deleted successfully"))
    }
}