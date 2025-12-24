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
import software.setixx.chimu.api.domain.GameJamStatus
import software.setixx.chimu.api.dto.ChangeGameJamStatusRequest
import software.setixx.chimu.api.dto.CreateGameJamRequest
import software.setixx.chimu.api.dto.GameJamDetailsResponse
import software.setixx.chimu.api.dto.GameJamResponse
import software.setixx.chimu.api.dto.UpdateGameJamRequest
import software.setixx.chimu.api.repository.UserRepository
import software.setixx.chimu.api.security.CustomUserDetails
import software.setixx.chimu.api.service.GameJamService

@RestController
@RequestMapping("/api/jams")
@Tag(name = "Game Jams", description = "Game jam management endpoints")
class GameJamController(
    private val gameJamService: GameJamService,
    private val userRepository: UserRepository
) {

    @PostMapping
    @Operation(summary = "Create a new game jam", description = "Creates a new game jam. Only organizers can create jams.")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Game jam created successfully"),
        ApiResponse(responseCode = "400", description = "Invalid request data"),
        ApiResponse(responseCode = "403", description = "User is not an organizer")
    )
    fun createGameJam(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Valid @RequestBody request: CreateGameJamRequest
    ): ResponseEntity<GameJamDetailsResponse> {
        val user = userRepository.findByPublicId(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        val jam = gameJamService.createGameJam(user.id!!, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(jam)
    }

    @GetMapping
    @Operation(summary = "Get all game jams", description = "Retrieves a list of all game jams, optionally filtered by status")
    @ApiResponse(responseCode = "200", description = "Game jams retrieved successfully")
    fun getAllGameJams(
        @Parameter(description = "Filter by game jam status")
        @RequestParam(required = false) status: GameJamStatus?
    ): ResponseEntity<List<GameJamResponse>> {
        val jams = gameJamService.getAllGameJams(status)
        return ResponseEntity.ok(jams)
    }

    @GetMapping("/{jamId}")
    @Operation(summary = "Get game jam by ID", description = "Retrieves detailed information about a specific game jam")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Game jam retrieved successfully"),
        ApiResponse(responseCode = "404", description = "Game jam not found")
    )
    fun getGameJamById(
        @Parameter(description = "Game jam public ID")
        @PathVariable jamId: String
    ): ResponseEntity<GameJamDetailsResponse> {
        val jam = gameJamService.getGameJamById(jamId)
        return ResponseEntity.ok(jam)
    }

    @PatchMapping("/{jamId}")
    @Operation(summary = "Update game jam", description = "Updates game jam details. Only the organizer or admin can update.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Game jam updated successfully"),
        ApiResponse(responseCode = "400", description = "Invalid request data"),
        ApiResponse(responseCode = "403", description = "Not authorized to update this game jam"),
        ApiResponse(responseCode = "404", description = "Game jam not found")
    )
    fun updateGameJam(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Parameter(description = "Game jam public ID")
        @PathVariable jamId: String,
        @Valid @RequestBody request: UpdateGameJamRequest
    ): ResponseEntity<GameJamDetailsResponse> {
        val user = userRepository.findByPublicId(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        val jam = gameJamService.updateGameJam(jamId, user.id!!, request)
        return ResponseEntity.ok(jam)
    }

    @DeleteMapping("/{jamId}")
    @Operation(summary = "Delete game jam", description = "Deletes a game jam. Only the organizer or admin can delete.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Game jam deleted successfully"),
        ApiResponse(responseCode = "403", description = "Not authorized to delete this game jam"),
        ApiResponse(responseCode = "404", description = "Game jam not found")
    )
    fun deleteGameJam(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Parameter(description = "Game jam public ID")
        @PathVariable jamId: String
    ): ResponseEntity<Map<String, String>> {
        val user = userRepository.findByPublicId(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        gameJamService.deleteGameJam(jamId, user.id!!)
        return ResponseEntity.ok(mapOf("message" to "Game jam deleted successfully"))
    }
}