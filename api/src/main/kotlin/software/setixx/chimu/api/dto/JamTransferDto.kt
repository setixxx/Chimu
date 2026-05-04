package software.setixx.chimu.api.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import software.setixx.chimu.api.domain.TransferStatus

data class CreateJamTransferRequest(
    @field:NotBlank(message = "Recipient user ID is required")
    val recipientUserId: String
)

data class ReviewJamTransferRequest(
    @field:NotNull(message = "Status is required")
    val status: TransferStatus
)

data class JamTransferRequestResponse(
    val id: String,
    val jamId: String,
    val jamName: String,
    val senderId: String,
    val senderNickname: String,
    val recipientId: String,
    val recipientNickname: String,
    val status: TransferStatus,
    val expiresAt: String,
    val createdAt: String
)