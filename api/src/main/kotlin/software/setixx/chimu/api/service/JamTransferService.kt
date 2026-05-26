package software.setixx.chimu.api.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import software.setixx.chimu.api.domain.JamTransferRequest
import software.setixx.chimu.api.domain.RegistrationStatus
import software.setixx.chimu.api.domain.TransferStatus
import software.setixx.chimu.api.domain.UserRole
import software.setixx.chimu.api.dto.CreateJamTransferRequest
import software.setixx.chimu.api.dto.JamTransferRequestResponse
import software.setixx.chimu.api.dto.ReviewJamTransferRequest
import software.setixx.chimu.api.repository.GameJamRepository
import software.setixx.chimu.api.repository.JamTeamRegistrationRepository
import software.setixx.chimu.api.repository.JamTransferRequestRepository
import software.setixx.chimu.api.repository.UserRepository
import java.time.Instant
import java.util.UUID

@Service
class JamTransferService(
    private val jamTransferRequestRepository: JamTransferRequestRepository,
    private val gameJamRepository: GameJamRepository,
    private val userRepository: UserRepository,
    private val jamRegistrationRepository: JamTeamRegistrationRepository
) {

    @Transactional
    fun createTransfer(jamId: String, senderId: Long, request: CreateJamTransferRequest): JamTransferRequestResponse {
        val jam = gameJamRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(jamId))
            ?: throw IllegalArgumentException("Game jam not found")

        if (jam.organizer.id != senderId) {
            throw IllegalArgumentException("Only the organizer can initiate a transfer")
        }

        val recipient = userRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(request.recipientUserId))
            ?: throw IllegalArgumentException("Recipient not found")

        if (recipient.role != UserRole.ORGANIZER && recipient.role != UserRole.ADMIN) {
            throw IllegalArgumentException("Recipient must have ORGANIZER role")
        }

        if (recipient.id == senderId) {
            throw IllegalArgumentException("Cannot transfer jam to yourself")
        }

        ensureRecipientIsNotParticipant(jam.id!!, recipient.id!!)

        val existing = jamTransferRequestRepository.findByJamIdAndStatusAndDeletedAtIsNull(jam.id!!, TransferStatus.PENDING)
        if (existing != null) {
            throw IllegalArgumentException("A pending transfer request already exists for this jam")
        }

        val sender = userRepository.findById(senderId).orElseThrow()

        val transferRequest = JamTransferRequest(
            jam = jam,
            sender = sender,
            recipient = recipient,
            status = TransferStatus.PENDING
        )

        val saved = jamTransferRequestRepository.save(transferRequest)
        return toResponse(saved)
    }

    @Transactional
    fun cancelTransfer(jamId: String, senderId: Long): JamTransferRequestResponse {
        val jam = gameJamRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(jamId))
            ?: throw IllegalArgumentException("Game jam not found")

        if (jam.organizer.id != senderId) {
            throw IllegalArgumentException("Only the organizer can cancel a transfer")
        }

        val transferRequest = jamTransferRequestRepository.findByJamIdAndStatusAndDeletedAtIsNull(jam.id!!, TransferStatus.PENDING)
            ?: throw IllegalArgumentException("No pending transfer request found for this jam")

        transferRequest.status = TransferStatus.CANCELLED
        return toResponse(jamTransferRequestRepository.save(transferRequest))
    }

    @Transactional(readOnly = true)
    fun getTransfers(userId: Long): List<JamTransferRequestResponse> {
        return jamTransferRequestRepository
            .findAllForUserWithVisibilityRules(userId, TransferStatus.CANCELLED)
            .map { toResponse(it) }
    }

    @Transactional
    fun reviewTransfer(requestId: Long, recipientId: Long, request: ReviewJamTransferRequest): JamTransferRequestResponse {
        if (request.status !in listOf(TransferStatus.ACCEPTED, TransferStatus.REJECTED)) {
            throw IllegalArgumentException("Status must be ACCEPTED or REJECTED")
        }

        val transferRequest = jamTransferRequestRepository.findById(requestId)
            .orElseThrow { IllegalArgumentException("Transfer request not found") }

        if (transferRequest.recipient.id != recipientId) {
            throw IllegalArgumentException("Not authorized to review this transfer")
        }

        if (transferRequest.status != TransferStatus.PENDING) {
            throw IllegalArgumentException("Only pending requests can be reviewed")
        }

        val expiresAt = transferRequest.expiresAt
        if (expiresAt != null && Instant.now().isAfter(expiresAt)) {
            throw IllegalArgumentException("Transfer request has expired")
        }

        transferRequest.status = request.status

        if (request.status == TransferStatus.ACCEPTED) {
            val jam = gameJamRepository.findById(transferRequest.jam.id!!).orElseThrow()
            ensureRecipientIsNotParticipant(jam.id!!, recipientId)
            jam.organizer.let {
                val recipient = userRepository.findById(transferRequest.recipient.id!!).orElseThrow()
                val updatedJam = gameJamRepository.findById(jam.id!!).orElseThrow()
                updatedJam.organizer = recipient
                gameJamRepository.save(updatedJam)

            }
        }

        return toResponse(jamTransferRequestRepository.save(transferRequest))
    }

    private fun ensureRecipientIsNotParticipant(jamId: Long, recipientId: Long) {
        if (jamRegistrationRepository.existsActiveRegistrationForUserInJam(jamId, recipientId, ACTIVE_REGISTRATION_STATUSES)) {
            throw IllegalArgumentException("Recipient cannot be a participant in this jam")
        }
    }

    companion object {
        private val ACTIVE_REGISTRATION_STATUSES = setOf(
            RegistrationStatus.PENDING,
            RegistrationStatus.APPROVED
        )
    }

    private fun toResponse(request: JamTransferRequest): JamTransferRequestResponse {
        return JamTransferRequestResponse(
            id = request.publicId.toString(),
            jamId = request.jam.publicId.toString(),
            jamName = request.jam.name,
            senderId = request.sender.publicId.toString(),
            senderNickname = request.sender.nickname,
            recipientId = request.recipient.publicId.toString(),
            recipientNickname = request.recipient.nickname,
            status = request.status,
            expiresAt = request.expiresAt.toString(),
            createdAt = request.createdAt.toString()
        )
    }
}
