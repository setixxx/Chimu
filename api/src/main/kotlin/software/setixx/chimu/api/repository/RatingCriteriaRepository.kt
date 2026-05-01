package software.setixx.chimu.api.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import software.setixx.chimu.api.domain.RatingCriteria

interface RatingCriteriaRepository : JpaRepository<RatingCriteria, Long> {
    @Query("""
        SELECT rc FROM RatingCriteria rc 
        WHERE rc.gameJam.id = :jamId
        AND rc.deletedAt IS NULL
        ORDER BY rc.orderIndex ASC
    """)
    fun findAllByJamIdOrderByOrderIndex(@Param("jamId") jamId: Long): List<RatingCriteria>

    fun existsByGameJamIdAndNameAndDeletedAtIsNull(jamId: Long, name: String): Boolean

    @Query("""
        SELECT COUNT(rc) FROM RatingCriteria rc 
        WHERE rc.gameJam.id = :jamId
        AND rc.deletedAt IS NULL
    """)
    fun countByJamId(@Param("jamId") jamId: Long): Long
}