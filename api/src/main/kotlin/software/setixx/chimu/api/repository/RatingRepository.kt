package software.setixx.chimu.api.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import software.setixx.chimu.api.domain.Rating

interface RatingRepository : JpaRepository<Rating, Long> {
    fun findByProjectIdAndJudgeIdAndCriteriaIdAndDeletedAtIsNull(projectId: Long, judgeId: Long, criteriaId: Long): Rating?
    fun findAllByProjectIdAndDeletedAtIsNull(projectId: Long): List<Rating>
    fun findAllByJudgeId(judgeId: Long): List<Rating>
    fun findAllByProjectIdAndJudgeIdAndDeletedAtIsNull(projectId: Long, judgeId: Long): List<Rating>

    @Query("""
        SELECT r FROM Rating r 
        WHERE r.project.id IN :projectIds
        AND r.deletedAt IS NULL
    """)
    fun findAllByProjectIdIn(@Param("projectIds") projectIds: List<Long>): List<Rating>

    @Query("""
        SELECT COUNT(DISTINCT r.project.id) FROM Rating r 
        WHERE r.judge.id = :judgeId 
        AND r.deletedAt IS NULL
        AND r.project.id IN (
            SELECT p.id FROM Project p WHERE p.gameJam.id = :jamId AND p.deletedAt IS NULL
        )
    """)
    fun countRatedProjectsByJudgeAndJam(
        @Param("judgeId") judgeId: Long,
        @Param("jamId") jamId: Long
    ): Long

    @Query("""
        SELECT COUNT(DISTINCT r.criteria.id) FROM Rating r 
        WHERE r.judge.id = :judgeId 
        AND r.project.id = :projectId
        AND r.deletedAt IS NULL
    """)
    fun countRatedCriteriaByJudgeAndProject(
        @Param("judgeId") judgeId: Long,
        @Param("projectId") projectId: Long
    ): Long

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Rating r SET r.deletedAt = CURRENT_TIMESTAMP WHERE r.id = :id")
    fun softDeleteById(@Param("id") id: Long)

    @Query("""
        SELECT r.judge.id FROM Rating r 
        WHERE r.project.id IN (
            SELECT p.id FROM Project p WHERE p.gameJam.id = :jamId AND p.deletedAt IS NULL
        )
        GROUP BY r.judge.id
    """)
    fun findJudgesWhoRatedInJam(@Param("jamId") jamId: Long): List<Long>
}