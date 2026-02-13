package software.setixx.chimu.api.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import software.setixx.chimu.api.domain.JamJudge

interface JamJudgeRepository : JpaRepository<JamJudge, Long> {
    fun findAllByJamId(jamId: Long): List<JamJudge>

    fun findAllByJudgeId(judgeId: Long): List<JamJudge>

    fun existsByJamIdAndJudgeId(jamId: Long, judgeId: Long): Boolean

    fun findByJamIdAndJudgeId(jamId: Long, judgeId: Long): JamJudge?

    fun deleteByJamIdAndJudgeId(jamId: Long, judgeId: Long)

    @Query("""
        SELECT COUNT(jj) FROM JamJudge jj 
        WHERE jj.jamId = :jamId
    """)
    fun countByJamId(@Param("jamId") jamId: Long): Long
}