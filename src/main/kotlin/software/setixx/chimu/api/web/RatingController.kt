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
import software.setixx.chimu.api.dto.*
import software.setixx.chimu.api.repository.UserRepository
import software.setixx.chimu.api.security.CustomUserDetails
import software.setixx.chimu.api.service.RatingService

@RestController
@RequestMapping("/api")
@Tag(name = "Ratings", description = "Project rating management for judges")
class RatingController(
    private val ratingService: RatingService,
    private val userRepository: UserRepository
) {

    @PostMapping("/projects/{projectId}/ratings")
    @Operation(summary = "Rate project", description = "Submits or updates a rating for a project. Only judges during JUDGING phase.")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Rating submitted successfully"),
        ApiResponse(responseCode = "400", description = "Invalid request or not allowed"),
        ApiResponse(responseCode = "403", description = "Not a judge for this jam")
    )
    fun rateProject(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Parameter(description = "Project public ID")
        @PathVariable projectId: String,
        @Valid @RequestBody request: RateProjectRequest
    ): ResponseEntity<RatingResponse> {
        val user = userRepository.findByPublicId(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        val rating = ratingService.rateProject(projectId, user.id!!, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(rating)
    }

    @PatchMapping("/ratings/{ratingId}")
    @Operation(summary = "Update rating", description = "Updates an existing rating. Only during JUDGING phase.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Rating updated successfully"),
        ApiResponse(responseCode = "403", description = "Not your rating or judging phase ended")
    )
    fun updateRating(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Parameter(description = "Rating ID")
        @PathVariable ratingId: Long,
        @Valid @RequestBody request: UpdateRatingRequest
    ): ResponseEntity<RatingResponse> {
        val user = userRepository.findByPublicId(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        val rating = ratingService.updateRating(ratingId, user.id!!, request)
        return ResponseEntity.ok(rating)
    }

    @DeleteMapping("/ratings/{ratingId}")
    @Operation(summary = "Delete rating", description = "Deletes a rating. Only during JUDGING phase.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Rating deleted successfully"),
        ApiResponse(responseCode = "403", description = "Not your rating or judging phase ended")
    )
    fun deleteRating(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Parameter(description = "Rating ID")
        @PathVariable ratingId: Long
    ): ResponseEntity<Map<String, String>> {
        val user = userRepository.findByPublicId(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        ratingService.deleteRating(ratingId, user.id!!)
        return ResponseEntity.ok(mapOf("message" to "Rating deleted successfully"))
    }

    @GetMapping("/projects/{projectId}/ratings")
    @Operation(summary = "Get project ratings", description = "Retrieves all ratings for a project. Only after COMPLETED.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Ratings retrieved successfully"),
        ApiResponse(responseCode = "403", description = "Ratings not yet visible")
    )
    fun getProjectRatings(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Parameter(description = "Project public ID")
        @PathVariable projectId: String
    ): ResponseEntity<ProjectRatingSummaryResponse> {
        val user = userRepository.findByPublicId(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        val ratings = ratingService.getProjectRatings(projectId, user.id!!)
        return ResponseEntity.ok(ratings)
    }

    @GetMapping("/projects/{projectId}/my-ratings")
    @Operation(summary = "Get my ratings", description = "Retrieves judge's own ratings for a project")
    @ApiResponse(responseCode = "200", description = "Ratings retrieved successfully")
    fun getMyRatings(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Parameter(description = "Project public ID")
        @PathVariable projectId: String
    ): ResponseEntity<List<MyRatingResponse>> {
        val user = userRepository.findByPublicId(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        val ratings = ratingService.getMyRatings(projectId, user.id!!)
        return ResponseEntity.ok(ratings)
    }

    @GetMapping("/jams/{jamId}/my-progress")
    @Operation(summary = "Get rating progress", description = "Shows judge's rating progress for a jam")
    @ApiResponse(responseCode = "200", description = "Progress retrieved successfully")
    fun getJudgeProgress(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Parameter(description = "Game jam public ID")
        @PathVariable jamId: String
    ): ResponseEntity<JudgeProgressResponse> {
        val user = userRepository.findByPublicId(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        val progress = ratingService.getJudgeProgress(jamId, user.id!!)
        return ResponseEntity.ok(progress)
    }
}