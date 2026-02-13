package software.setixx.chimu.api.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import software.setixx.chimu.api.domain.Rating

interface RatingRepository : JpaRepository<Rating, Long> {
    fun findByProjectIdAndJudgeIdAndCriteriaId(
        projectId: Long,
        judgeId: Long,
        criteriaId: Long
    ): Rating?

    fun findAllByProjectId(projectId: Long): List<Rating>

    fun findAllByJudgeId(judgeId: Long): List<Rating>

    fun findAllByProjectIdAndJudgeId(projectId: Long, judgeId: Long): List<Rating>

    @Query("""
        SELECT r FROM Rating r 
        WHERE r.projectId IN :projectIds
    """)
    fun findAllByProjectIdIn(@Param("projectIds") projectIds: List<Long>): List<Rating>

    @Query("""
        SELECT COUNT(DISTINCT r.projectId) FROM Rating r 
        WHERE r.judgeId = :judgeId 
        AND r.projectId IN (
            SELECT p.id FROM Project p WHERE p.jamId = :jamId
        )
    """)
    fun countRatedProjectsByJudgeAndJam(
        @Param("judgeId") judgeId: Long,
        @Param("jamId") jamId: Long
    ): Long

    @Query("""
        SELECT COUNT(DISTINCT r.criteriaId) FROM Rating r 
        WHERE r.judgeId = :judgeId 
        AND r.projectId = :projectId
    """)
    fun countRatedCriteriaByJudgeAndProject(
        @Param("judgeId") judgeId: Long,
        @Param("projectId") projectId: Long
    ): Long

    @Modifying
    @Query("""
        DELETE FROM Rating r 
        WHERE r.judgeId = :judgeId
    """)
    fun deleteAllByJudgeId(@Param("judgeId") judgeId: Long): Int

    @Query("""
        SELECT r.judgeId FROM Rating r 
        WHERE r.projectId IN (
            SELECT p.id FROM Project p WHERE p.jamId = :jamId
        )
        GROUP BY r.judgeId
    """)
    fun findJudgesWhoRatedInJam(@Param("jamId") jamId: Long): List<Long>
}