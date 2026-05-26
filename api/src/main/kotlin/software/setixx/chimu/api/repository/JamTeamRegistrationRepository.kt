package software.setixx.chimu.api.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import software.setixx.chimu.api.domain.JamTeamRegistration
import software.setixx.chimu.api.domain.RegistrationStatus

interface JamTeamRegistrationRepository : JpaRepository<JamTeamRegistration, Long> {
    fun findByGameJamIdAndTeamIdAndDeletedAtIsNull(jamId: Long, teamId: Long): JamTeamRegistration?

    fun findAllByGameJamIdAndDeletedAtIsNull(jamId: Long): List<JamTeamRegistration>

    fun findAllByTeamIdAndDeletedAtIsNull(teamId: Long): List<JamTeamRegistration>

    fun existsByGameJamIdAndTeamIdAndDeletedAtIsNull(jamId: Long, teamId: Long): Boolean

    fun existsByGameJamIdAndRegisteredByAndDeletedAtIsNull(jamId: Long, registeredBy: Long): Boolean

    @Query("""
        SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
        FROM JamTeamRegistration r
        JOIN TeamMember tm ON tm.team = r.team
        WHERE r.gameJam.id = :jamId
        AND tm.user.id = :userId
        AND r.status IN :statuses
        AND r.deletedAt IS NULL
        AND tm.deletedAt IS NULL
    """)
    fun existsActiveRegistrationForUserInJam(
        @Param("jamId") jamId: Long,
        @Param("userId") userId: Long,
        @Param("statuses") statuses: Set<RegistrationStatus>
    ): Boolean

    @Query("""
        SELECT COUNT(r) FROM JamTeamRegistration r 
        WHERE r.gameJam.id = :jamId 
        AND r.status IN ('APPROVED', 'PENDING')
        AND r.deletedAt IS NULL
    """)
    fun countRegisteredTeams(@Param("jamId") jamId: Long): Long

    fun findAllByGameJamIdAndStatusAndDeletedAtIsNull(jamId: Long,status: RegistrationStatus): List<JamTeamRegistration>

    @Query("""
        SELECT r FROM JamTeamRegistration r 
        WHERE r.team.id = :teamId 
        AND r.status IN ('APPROVED', 'PENDING')
        AND r.deletedAt IS NULL
    """)
    fun findActiveRegistrationsByTeamId(@Param("teamId") teamId: Long): List<JamTeamRegistration>

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE JamTeamRegistration r SET r.deletedAt = CURRENT_TIMESTAMP WHERE r.id = :id")
    fun softDeleteById(@Param("id") id: Long)
}
