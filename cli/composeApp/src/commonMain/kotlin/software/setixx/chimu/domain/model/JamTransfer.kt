package software.setixx.chimu.domain.model

import software.setixx.chimu.api.domain.TransferStatus

data class CreateJamTransfer(
    val recipientUserId: String
)

data class ReviewJamTransfer(
    val status: TransferStatus
)

data class JamTransfer(
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