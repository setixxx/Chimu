package setixx.software.Chimu.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import setixx.software.Chimu.domain.Team
import java.util.UUID

interface TeamRepository : JpaRepository<Team, Long> {
    fun findByPublicId(publicId: UUID): Team?

    fun findByInviteToken(inviteToken: String): Team?

    fun findAllByLeaderId(leaderId: Long): List<Team>

    @Query("""
        SELECT t FROM Team t 
        JOIN TeamMember tm ON t.id = tm.teamId 
        WHERE tm.userId = :userId
    """)
    fun findAllByMemberId(@Param("userId") userId: Long): List<Team>
}