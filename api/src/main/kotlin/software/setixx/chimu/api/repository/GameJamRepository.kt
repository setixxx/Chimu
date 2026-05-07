package software.setixx.chimu.api.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import software.setixx.chimu.api.domain.GameJam
import software.setixx.chimu.api.domain.GameJamStatus
import java.time.Instant
import java.util.UUID

interface GameJamRepository : JpaRepository<GameJam, Long> {
    fun findByPublicIdAndDeletedAtIsNull(publicId: UUID): GameJam?
    fun findAllByDeletedAtIsNull(): List<GameJam>
    fun findAllByStatusAndDeletedAtIsNull(status: GameJamStatus): List<GameJam>
    fun existsByNameAndDeletedAtIsNull(name: String): Boolean

    fun findAllByOrganizerIdAndDeletedAtIsNull(organizerId: Long): List<GameJam>

    @Query("""
        SELECT gj FROM GameJam gj 
        WHERE gj.status IN :statuses 
        AND gj.deletedAt IS NULL 
        ORDER BY gj.registrationStart ASC
    """)
    fun findAllByStatusIn(@Param("statuses") statuses: List<GameJamStatus>): List<GameJam>

    @Query("""
        SELECT gj FROM GameJam gj 
        WHERE gj.registrationStart > :now 
        AND gj.status = 'REGISTRATION_OPEN'
        AND gj.deletedAt IS NULL
        ORDER BY gj.registrationStart ASC
    """)
    fun findUpcomingJams(@Param("now") now: Instant = Instant.now()): List<GameJam>

    @Query("""
        SELECT gj FROM GameJam gj 
        WHERE gj.status = 'IN_PROGRESS' 
        AND gj.jamStart <= :now 
        AND gj.jamEnd >= :now
        AND gj.deletedAt IS NULL
        ORDER BY gj.jamEnd ASC
    """)
    fun findActiveJams(@Param("now") now: Instant = Instant.now()): List<GameJam>

    @Query("""
        SELECT gj FROM GameJam gj 
        WHERE (gj.status = 'REGISTRATION_OPEN' OR gj.status = 'ANNOUNCED')
        AND gj.deletedAt IS NULL
        AND gj.registrationEnd <= :now
    """)
    fun findJamsToCloseRegistration(@Param("now") now: Instant = Instant.now()): List<GameJam>

    @Query("""
        SELECT gj FROM GameJam gj 
        WHERE gj.status = 'REGISTRATION_CLOSED'
        AND gj.deletedAt IS NULL
        AND gj.jamStart <= :now
    """)
    fun findJamsToStart(@Param("now") now: Instant = Instant.now()): List<GameJam>

    @Query("""
        SELECT gj FROM GameJam gj 
        WHERE gj.status = 'IN_PROGRESS' 
        AND gj.deletedAt IS NULL
        AND gj.judgingStart <= :now
    """)
    fun findJamsToStartJudging(@Param("now") now: Instant = Instant.now()): List<GameJam>

    @Query("""
        SELECT gj FROM GameJam gj 
        WHERE gj.status = 'JUDGING'
        AND gj.deletedAt IS NULL
        AND gj.judgingEnd <= :now
    """)
    fun findJamsToComplete(@Param("now") now: Instant = Instant.now()): List<GameJam>

    fun existsByOrganizerIdAndDeletedAtIsNullAndStatusNotIn(organizerId: Long, statuses: List<GameJamStatus>): Boolean

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE GameJam gj SET gj.deletedAt = CURRENT_TIMESTAMP WHERE gj.id = :id")
    fun softDeleteById(@Param("id") id: Long)
}