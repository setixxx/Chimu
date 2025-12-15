package setixx.software.Chimu.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import setixx.software.Chimu.domain.RatingCriteria

interface RatingCriteriaRepository : JpaRepository<RatingCriteria, Long> {
    @Query("""
        SELECT rc FROM RatingCriteria rc 
        WHERE rc.jamId = :jamId 
        ORDER BY rc.orderIndex ASC
    """)
    fun findAllByJamIdOrderByOrderIndex(@Param("jamId") jamId: Long): List<RatingCriteria>

    fun existsByJamIdAndName(jamId: Long, name: String): Boolean

    @Query("""
        SELECT COUNT(rc) FROM RatingCriteria rc 
        WHERE rc.jamId = :jamId
    """)
    fun countByJamId(@Param("jamId") jamId: Long): Long
}