package software.setixx.chimu.api.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import software.setixx.chimu.api.domain.TeamMember
import java.time.Instant

interface TeamMemberRepository : JpaRepository<TeamMember, Long> {
    fun findByTeamIdAndUserIdAndDeletedAtIsNull(teamId: Long, userId: Long): TeamMember?

    fun findAllByTeamIdAndDeletedAtIsNull(teamId: Long): List<TeamMember>

    fun findAllByUserIdAndDeletedAtIsNull(userId: Long): List<TeamMember>
    fun existsByTeamIdAndUserIdAndDeletedAtIsNull(teamId: Long, userId: Long): Boolean

    @Query("SELECT COUNT(tm) FROM TeamMember tm WHERE tm.team.id = :teamId AND tm.deletedAt IS NULL")
    fun countByTeamId(@Param("teamId") teamId: Long): Long

    @Query("""
    SELECT COUNT(tm) > 0 FROM TeamMember tm 
    WHERE tm.team.id = :teamId 
    AND tm.user.id = :userId 
    AND tm.joinedAt <= :projectCreatedAt
    AND (tm.deletedAt IS NULL OR tm.deletedAt > :projectCreatedAt)
    """)
    fun wasUserMemberAtTime(
        @Param("teamId") teamId: Long,
        @Param("userId") userId: Long,
        @Param("projectCreatedAt") projectCreatedAt: Instant
    ): Boolean

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE TeamMember tm SET tm.deletedAt = CURRENT_TIMESTAMP WHERE tm.team.id = :teamId AND tm.user.id = :userId AND tm.deletedAt IS NULL")
    fun softDeleteByTeamIdAndUserId(@Param("teamId") teamId: Long, @Param("userId") userId: Long)

    fun findByTeamIdAndUserId(teamId: Long, userId: Long): TeamMember?
    fun findAllByTeamId(teamId: Long): List<TeamMember>
}