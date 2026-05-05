package software.setixx.chimu.data.remote.dto

import kotlinx.serialization.Serializable
import software.setixx.chimu.api.domain.TransferStatus

@Serializable
data class CreateJamTransferRequest(
    val recipientUserId: String
)

@Serializable
data class ReviewJamTransferRequest(
    val status: TransferStatus
)

@Serializable
data class JamTransferResponse(
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