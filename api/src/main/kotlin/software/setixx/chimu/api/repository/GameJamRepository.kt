package software.setixx.chimu.api.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import software.setixx.chimu.api.domain.GameJam
import software.setixx.chimu.api.domain.GameJamStatus
import java.time.Instant
import java.util.UUID

interface GameJamRepository : JpaRepository<GameJam, Long> {
    fun findByPublicId(publicId: UUID): GameJam?

    fun findAllByStatus(status: GameJamStatus): List<GameJam>

    fun findAllByOrganizerId(organizerId: Long): List<GameJam>

    fun existsByName(name: String): Boolean

    @Query("""
        SELECT gj FROM GameJam gj 
        WHERE gj.status IN :statuses 
        ORDER BY gj.registrationStart ASC
    """)
    fun findAllByStatusIn(@Param("statuses") statuses: List<GameJamStatus>): List<GameJam>

    @Query("""
        SELECT gj FROM GameJam gj 
        WHERE gj.registrationStart > :now 
        AND gj.status = 'REGISTRATION_OPEN'
        ORDER BY gj.registrationStart ASC
    """)
    fun findUpcomingJams(@Param("now") now: Instant = Instant.now()): List<GameJam>

    @Query("""
        SELECT gj FROM GameJam gj 
        WHERE gj.status = 'IN_PROGRESS' 
        AND gj.jamStart <= :now 
        AND gj.jamEnd >= :now
        ORDER BY gj.jamEnd ASC
    """)
    fun findActiveJams(@Param("now") now: Instant = Instant.now()): List<GameJam>

    @Query("""
        SELECT gj FROM GameJam gj 
        WHERE gj.status = 'REGISTRATION_OPEN' 
        AND gj.registrationEnd <= :now
    """)
    fun findJamsToCloseRegistration(@Param("now") now: Instant = Instant.now()): List<GameJam>

    @Query("""
        SELECT gj FROM GameJam gj 
        WHERE gj.status = 'REGISTRATION_CLOSED' 
        AND gj.jamStart <= :now
    """)
    fun findJamsToStart(@Param("now") now: Instant = Instant.now()): List<GameJam>

    @Query("""
        SELECT gj FROM GameJam gj 
        WHERE gj.status = 'IN_PROGRESS' 
        AND gj.judgingStart <= :now
    """)
    fun findJamsToStartJudging(@Param("now") now: Instant = Instant.now()): List<GameJam>

    @Query("""
        SELECT gj FROM GameJam gj 
        WHERE gj.status = 'JUDGING' 
        AND gj.judgingEnd <= :now
    """)
    fun findJamsToComplete(@Param("now") now: Instant = Instant.now()): List<GameJam>
}