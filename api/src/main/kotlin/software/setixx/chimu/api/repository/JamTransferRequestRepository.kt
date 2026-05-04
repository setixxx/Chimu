package software.setixx.chimu.api.repository

import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import software.setixx.chimu.api.domain.JamTransferRequest
import software.setixx.chimu.api.domain.TransferStatus

interface JamTransferRequestRepository : JpaRepository<JamTransferRequest, Long> {
    fun findByJamIdAndStatusAndDeletedAtIsNull(jamId: Long, status: TransferStatus): JamTransferRequest?
    @Query("""
        SELECT jtr FROM JamTransferRequest jtr 
        WHERE jtr.deletedAt IS NULL
        AND (
            (jtr.sender.id = :userId)
            OR 
            (jtr.recipient.id = :userId AND jtr.status != :cancelledStatus)
        )
    """)
    fun findAllForUserWithVisibilityRules(
        @Param("userId") userId: Long,
        @Param("cancelledStatus") cancelledStatus: TransferStatus
    ): List<JamTransferRequest>
    fun findByPublicIdAndDeletedAtIsNull(publicId: UUID): JamTransferRequest?
}