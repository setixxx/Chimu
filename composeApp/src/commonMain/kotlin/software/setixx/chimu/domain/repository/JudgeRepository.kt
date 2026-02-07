package software.setixx.chimu.domain.repository

import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.model.AssignJudge
import software.setixx.chimu.domain.model.Judge

interface JudgeRepository {
    suspend fun getJamJudges(jamId: String): ApiResult<List<Judge>>
    suspend fun assignJudge(jamId: String, data: AssignJudge): ApiResult<Judge>
    suspend fun unassignJudge(jamId: String, judgeUserId: String): ApiResult<Unit>
}