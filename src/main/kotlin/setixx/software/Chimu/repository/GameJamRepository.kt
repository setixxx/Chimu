package setixx.software.Chimu.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import setixx.software.Chimu.domain.GameJam
import setixx.software.Chimu.domain.GameJamStatus
import java.time.Instant
import java.util.UUID

interface GameJamRepository : JpaRepository<GameJam, Long> {
    fun findByPublicId(publicId: UUID): GameJam?

    fun findAllByStatus(status: GameJamStatus): List<GameJam>

    fun findAllByOrganizerId(organizerId: Long): List<GameJam>

    @Query("""
        SELECT gj FROM GameJam gj 
        WHERE gj.status IN :statuses 
        ORDER BY gj.startDate ASC
    """)
    fun findAllByStatusIn(@Param("statuses") statuses: List<GameJamStatus>): List<GameJam>

    @Query("""
        SELECT gj FROM GameJam gj 
        WHERE gj.startDate > :now 
        AND gj.status IN ('ANNOUNCED', 'DRAFT')
        ORDER BY gj.startDate ASC
    """)
    fun findUpcomingJams(@Param("now") now: Instant = Instant.now()): List<GameJam>

    @Query("""
        SELECT gj FROM GameJam gj 
        WHERE gj.status = 'IN_PROGRESS' 
        AND gj.startDate <= :now 
        AND gj.endDate >= :now
        ORDER BY gj.endDate ASC
    """)
    fun findActiveJams(@Param("now") now: Instant = Instant.now()): List<GameJam>

    @Query("""
        SELECT gj FROM GameJam gj 
        WHERE gj.status = 'ANNOUNCED' 
        AND gj.startDate <= :now
    """)
    fun findJamsToStart(@Param("now") now: Instant = Instant.now()): List<GameJam>

    @Query("""
        SELECT gj FROM GameJam gj 
        WHERE gj.status = 'IN_PROGRESS' 
        AND gj.submissionDeadline <= :now
    """)
    fun findJamsToStartJudging(@Param("now") now: Instant = Instant.now()): List<GameJam>
}