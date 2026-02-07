package software.setixx.chimu.domain.repository

import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.model.CreateGameJam
import software.setixx.chimu.domain.model.GameJam
import software.setixx.chimu.domain.model.GameJamDetails
import software.setixx.chimu.domain.model.UpdateGameJam

interface GameJamRepository {
    suspend fun getAllJams(): ApiResult<List<GameJam>>
    suspend fun getActiveJams(): ApiResult<List<GameJam>>
    suspend fun createJam(data: CreateGameJam): ApiResult<GameJamDetails>
    suspend fun getJamDetails(gameJamId: String): ApiResult<GameJamDetails>
    suspend fun deleteJam(gameJamId: String): ApiResult<Unit>
    suspend fun updateJam(gameJamId: String, data: UpdateGameJam): ApiResult<GameJamDetails>
}