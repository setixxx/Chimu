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
import software.setixx.chimu.api.dto.AssignJudgeRequest
import software.setixx.chimu.api.dto.JudgeResponse
import software.setixx.chimu.api.repository.UserRepository
import software.setixx.chimu.api.security.CustomUserDetails
import software.setixx.chimu.api.service.JudgeManagementService

@RestController
@RequestMapping("/api/jams/{jamId}/judges")
@Tag(name = "Judges", description = "Judge management for game jams")
class JudgeController(
    private val judgeManagementService: JudgeManagementService,
    private val userRepository: UserRepository
) {

    @PostMapping
    @Operation(summary = "Assign judge", description = "Assigns a judge to a game jam")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Judge assigned successfully"),
        ApiResponse(responseCode = "400", description = "Invalid request or judge already assigned"),
        ApiResponse(responseCode = "403", description = "Not authorized to assign judges")
    )
    fun assignJudge(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Parameter(description = "Game jam public ID")
        @PathVariable jamId: String,
        @Valid @RequestBody request: AssignJudgeRequest
    ): ResponseEntity<JudgeResponse> {
        val user = userRepository.findByPublicId(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        val judge = judgeManagementService.assignJudge(jamId, user.id!!, request.judgeUserId)
        return ResponseEntity.status(HttpStatus.CREATED).body(judge)
    }

    @GetMapping
    @Operation(summary = "Get jam judges", description = "Retrieves all judges assigned to a game jam")
    @ApiResponse(responseCode = "200", description = "Judges retrieved successfully")
    fun getJamJudges(
        @Parameter(description = "Game jam public ID")
        @PathVariable jamId: String
    ): ResponseEntity<List<JudgeResponse>> {
        val judges = judgeManagementService.getJamJudges(jamId)
        return ResponseEntity.ok(judges)
    }

    @DeleteMapping("/{judgeUserId}")
    @Operation(summary = "Remove judge", description = "Removes a judge from a game jam")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Judge removed successfully"),
        ApiResponse(responseCode = "403", description = "Not authorized to remove judges"),
        ApiResponse(responseCode = "404", description = "Judge not found")
    )
    fun removeJudge(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Parameter(description = "Game jam public ID")
        @PathVariable jamId: String,
        @Parameter(description = "Judge user public ID")
        @PathVariable judgeUserId: String
    ): ResponseEntity<Map<String, String>> {
        val user = userRepository.findByPublicId(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        judgeManagementService.removeJudge(jamId, user.id!!, judgeUserId)
        return ResponseEntity.ok(mapOf("message" to "Judge removed successfully"))
    }
}