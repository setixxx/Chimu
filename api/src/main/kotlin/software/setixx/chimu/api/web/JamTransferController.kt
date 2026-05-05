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
import software.setixx.chimu.api.dto.CreateJamTransferRequest
import software.setixx.chimu.api.dto.JamTransferRequestResponse
import software.setixx.chimu.api.dto.ReviewJamTransferRequest
import software.setixx.chimu.api.repository.JamTransferRequestRepository
import software.setixx.chimu.api.repository.UserRepository
import software.setixx.chimu.api.security.CustomUserDetails
import software.setixx.chimu.api.service.JamTransferService
import java.util.UUID

@RestController
@Tag(name = "Jam Transfers", description = "Game jam ownership transfer management")
class JamTransferController(
    private val jamTransferService: JamTransferService,
    private val userRepository: UserRepository,
    private val jamTransferRequestRepository: JamTransferRequestRepository
) {

    @PostMapping("/api/jams/{jamId}/transfer")
    @Operation(summary = "Create transfer request", description = "Creates a request to transfer game jam ownership")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Transfer request created successfully"),
        ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        ApiResponse(responseCode = "403", description = "Not authorized to transfer this game jam"),
        ApiResponse(responseCode = "404", description = "Game jam not found")
    )
    fun createTransfer(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Parameter(description = "Game jam public ID")
        @PathVariable jamId: String,
        @Valid @RequestBody request: CreateJamTransferRequest
    ): ResponseEntity<JamTransferRequestResponse> {
        val user = userRepository.findByPublicIdAndDeletedAtIsNull(userDetails.publicId)
            ?: throw IllegalStateException("User not found")
        val response = jamTransferService.createTransfer(jamId, user.id!!, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @DeleteMapping("/api/jams/{jamId}/transfer")
    @Operation(summary = "Cancel transfer request", description = "Cancels an active request to transfer game jam ownership")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Transfer request cancelled successfully"),
        ApiResponse(responseCode = "403", description = "Not authorized to cancel this request"),
        ApiResponse(responseCode = "404", description = "Game jam or transfer request not found")
    )
    fun cancelTransfer(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Parameter(description = "Game jam public ID")
        @PathVariable jamId: String
    ): ResponseEntity<JamTransferRequestResponse> {
        val user = userRepository.findByPublicIdAndDeletedAtIsNull(userDetails.publicId)
            ?: throw IllegalStateException("User not found")
        return ResponseEntity.ok(jamTransferService.cancelTransfer(jamId, user.id!!))
    }

    @GetMapping("/api/users/me/transfer-requests")
    @Operation(summary = "Get user transfer requests", description = "Retrieves all game jam transfer requests related to the current user")
    @ApiResponse(responseCode = "200", description = "Transfer requests retrieved successfully")
    fun getTransfers(
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<List<JamTransferRequestResponse>> {
        val user = userRepository.findByPublicIdAndDeletedAtIsNull(userDetails.publicId)
            ?: throw IllegalStateException("User not found")
        return ResponseEntity.ok(jamTransferService.getTransfers(user.id!!))
    }

    @PatchMapping("/api/transfer-requests/{requestId}")
    @Operation(summary = "Review transfer request", description = "Approves or rejects an incoming game jam transfer request")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Transfer request reviewed successfully"),
        ApiResponse(responseCode = "400", description = "Invalid review request"),
        ApiResponse(responseCode = "403", description = "Not authorized to review this request"),
        ApiResponse(responseCode = "404", description = "Transfer request not found")
    )
    fun reviewTransfer(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Parameter(description = "Transfer request public ID")
        @PathVariable requestId: String,
        @Valid @RequestBody request: ReviewJamTransferRequest
    ): ResponseEntity<JamTransferRequestResponse> {
        val user = userRepository.findByPublicIdAndDeletedAtIsNull(userDetails.publicId)
            ?: throw IllegalStateException("User not found")
        val transferRequest = jamTransferRequestRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(requestId))
            ?: throw IllegalStateException("Transfer request not found")
        return ResponseEntity.ok(jamTransferService.reviewTransfer(transferRequest.id!!, user.id!!, request))
    }
}