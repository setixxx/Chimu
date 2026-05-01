package software.setixx.chimu.api.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import software.setixx.chimu.api.domain.JamJudge

interface JamJudgeRepository : JpaRepository<JamJudge, Long> {
    fun findAllByGameJamIdAndDeletedAtIsNull(jamId: Long): List<JamJudge>

    fun findAllByJudgeId(judgeId: Long): List<JamJudge>

    fun existsByGameJamIdAndJudgeIdAndDeletedAtIsNull(jamId: Long, judgeId: Long): Boolean

    fun findByGameJamIdAndJudgeIdAndDeletedAtIsNull(jamId: Long, judgeId: Long): JamJudge?

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE JamJudge jj SET jj.deletedAt = CURRENT_TIMESTAMP WHERE jj.gameJam.id = :jamId AND jj.judge.id = :judgeId AND jj.deletedAt IS NULL")
    fun softDeleteByJamIdAndJudgeId(@Param("jamId") jamId: Long, @Param("judgeId") judgeId: Long)

    @Query("""
        SELECT COUNT(jj) FROM JamJudge jj 
        WHERE jj.gameJam.id = :jamId
        AND jj.deletedAt IS NULL
    """)
    fun countByJamId(@Param("jamId") jamId: Long): Long

    @Query("""
    SELECT CASE WHEN COUNT(jj) > 0 THEN true ELSE false END 
    FROM JamJudge jj
    JOIN jj.gameJam gj
    WHERE jj.judge.id = :judgeId
      AND gj.status NOT IN ('COMPLETED', 'CANCELLED')
      AND gj.deletedAt IS NULL
      AND jj.deletedAt IS NULL
""")
    fun isJudgeInOngoingJam(@Param("judgeId") judgeId: Long): Boolean
}