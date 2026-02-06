package software.setixx.chimu.domain.repository

import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.model.GameJam

interface GameJamRepository {
    suspend fun getAllJams(): ApiResult<List<GameJam>>
    suspend fun getActiveJams(): ApiResult<List<GameJam>>
}