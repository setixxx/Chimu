package software.setixx.chimu.api.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import software.setixx.chimu.api.domain.TeamMember

interface TeamMemberRepository : JpaRepository<TeamMember, Long> {
    fun findByTeamIdAndUserId(teamId: Long, userId: Long): TeamMember?

    fun findAllByTeamId(teamId: Long): List<TeamMember>

    fun findAllByUserId(userId: Long): List<TeamMember>

    fun existsByTeamIdAndUserId(teamId: Long, userId: Long): Boolean

    fun deleteByTeamIdAndUserId(teamId: Long, userId: Long)

    @Query("SELECT COUNT(tm) FROM TeamMember tm WHERE tm.teamId = :teamId")
    fun countByTeamId(@Param("teamId") teamId: Long): Long
}