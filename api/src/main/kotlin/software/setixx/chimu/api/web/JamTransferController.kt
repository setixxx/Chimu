package software.setixx.chimu.api.web

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
class JamTransferController(
    private val jamTransferService: JamTransferService,
    private val userRepository: UserRepository,
    private val jamTransferRequestRepository: JamTransferRequestRepository
) {

    @PostMapping("/api/jams/{jamId}/transfer")
    fun createTransfer(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PathVariable jamId: String,
        @Valid @RequestBody request: CreateJamTransferRequest
    ): ResponseEntity<JamTransferRequestResponse> {
        val user = userRepository.findByPublicIdAndDeletedAtIsNull(userDetails.publicId)
            ?: throw IllegalStateException("User not found")
        val response = jamTransferService.createTransfer(jamId, user.id!!, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @DeleteMapping("/api/jams/{jamId}/transfer")
    fun cancelTransfer(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PathVariable jamId: String
    ): ResponseEntity<JamTransferRequestResponse> {
        val user = userRepository.findByPublicIdAndDeletedAtIsNull(userDetails.publicId)
            ?: throw IllegalStateException("User not found")
        return ResponseEntity.ok(jamTransferService.cancelTransfer(jamId, user.id!!))
    }

    @GetMapping("/api/users/me/transfer-requests")
    fun getTransfers(
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<List<JamTransferRequestResponse>> {
        val user = userRepository.findByPublicIdAndDeletedAtIsNull(userDetails.publicId)
            ?: throw IllegalStateException("User not found")
        return ResponseEntity.ok(jamTransferService.getTransfers(user.id!!))
    }

    @PatchMapping("/api/transfer-requests/{requestId}")
    fun reviewTransfer(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
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