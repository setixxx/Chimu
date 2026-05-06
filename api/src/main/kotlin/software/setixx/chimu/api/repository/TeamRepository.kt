package software.setixx.chimu.api.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import software.setixx.chimu.api.domain.Team
import java.util.UUID

interface TeamRepository : JpaRepository<Team, Long> {
    fun findByPublicIdAndDeletedAtIsNull(publicId: UUID): Team?
    fun findByInviteTokenAndDeletedAtIsNull(inviteToken: String): Team?
    fun findByNameAndDeletedAtIsNull(name: String): Team?
    fun findByLeaderIdAndDeletedAtIsNull(leaderId: Long): Team?

    fun findByPublicId(publicId: UUID): Team?
    fun findByName(name: String): Team?
    fun findAllByLeaderId(leaderId: Long): List<Team>

    fun findAllByLeaderIdAndDeletedAtIsNull(leaderId: Long): List<Team>

    @Query("SELECT t FROM Team t JOIN TeamMember tm ON t.id = tm.team.id WHERE tm.user.id = :userId AND t.deletedAt IS NULL AND tm.deletedAt IS NULL")
    fun findAllActiveByMemberId(@Param("userId") userId: Long): List<Team>

    fun existsByLeaderIdAndDeletedAtIsNull(leaderId: Long): Boolean

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Team t SET t.deletedAt = CURRENT_TIMESTAMP WHERE t.id = :id")
    fun softDeleteById(@Param("id") id: Long)
}