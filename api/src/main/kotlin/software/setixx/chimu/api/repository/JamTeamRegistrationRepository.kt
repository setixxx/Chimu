package software.setixx.chimu.api.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import software.setixx.chimu.api.domain.JamTeamRegistration
import software.setixx.chimu.api.domain.RegistrationStatus

interface JamTeamRegistrationRepository : JpaRepository<JamTeamRegistration, Long> {
    fun findByJamIdAndTeamId(jamId: Long, teamId: Long): JamTeamRegistration?

    fun findAllByJamId(jamId: Long): List<JamTeamRegistration>

    fun findAllByTeamId(teamId: Long): List<JamTeamRegistration>

    fun existsByJamIdAndTeamId(jamId: Long, teamId: Long): Boolean

    @Query("""
        SELECT COUNT(r) FROM JamTeamRegistration r 
        WHERE r.jamId = :jamId 
        AND r.status IN ('APPROVED', 'PENDING')
    """)
    fun countRegisteredTeams(@Param("jamId") jamId: Long): Long

    @Query("""
        SELECT r FROM JamTeamRegistration r 
        WHERE r.jamId = :jamId 
        AND r.status = :status
    """)
    fun findAllByJamIdAndStatus(
        @Param("jamId") jamId: Long,
        @Param("status") status: RegistrationStatus
    ): List<JamTeamRegistration>

    @Query("""
        SELECT r FROM JamTeamRegistration r 
        WHERE r.teamId = :teamId 
        AND r.status IN ('APPROVED', 'PENDING')
    """)
    fun findActiveRegistrationsByTeamId(@Param("teamId") teamId: Long): List<JamTeamRegistration>
}