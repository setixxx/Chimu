package software.setixx.chimu.api.web

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import software.setixx.chimu.api.dto.JamStatisticsResponse
import software.setixx.chimu.api.dto.LeaderboardResponse
import software.setixx.chimu.api.repository.UserRepository
import software.setixx.chimu.api.security.CustomUserDetails
import software.setixx.chimu.api.service.LeaderboardService

@RestController
@RequestMapping("/api/jams")
@Tag(name = "Leaderboard", description = "Jam leaderboard and statistics")
class LeaderboardController(
    private val leaderboardService: LeaderboardService,
    private val userRepository: UserRepository
) {

    @GetMapping("/{jamId}/leaderboard")
    @Operation(
        summary = "Get leaderboard",
        description = "Retrieves the leaderboard for a jam. Visible to organizers during JUDGING, everyone after COMPLETED."
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Leaderboard retrieved successfully"),
        ApiResponse(responseCode = "403", description = "Leaderboard not yet visible")
    )
    fun getLeaderboard(
        @AuthenticationPrincipal userDetails: CustomUserDetails?,
        @Parameter(description = "Game jam public ID")
        @PathVariable jamId: String
    ): ResponseEntity<LeaderboardResponse> {
        val userId = userDetails?.let {
            userRepository.findByPublicId(it.publicId)?.id
        }

        val leaderboard = leaderboardService.getLeaderboard(jamId, userId)
        return ResponseEntity.ok(leaderboard)
    }

    @GetMapping("/{jamId}/statistics")
    @Operation(
        summary = "Get jam statistics",
        description = "Retrieves detailed statistics for a jam. Organizers/admins only."
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
        ApiResponse(responseCode = "403", description = "Not authorized")
    )
    fun getStatistics(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Parameter(description = "Game jam public ID")
        @PathVariable jamId: String
    ): ResponseEntity<JamStatisticsResponse> {
        val user = userRepository.findByPublicId(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        val statistics = leaderboardService.getJamStatistics(jamId, user.id!!)
        return ResponseEntity.ok(statistics)
    }
}