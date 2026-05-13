package software.setixx.chimu.domain.repository

import kotlinx.coroutines.flow.Flow
import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.model.CreateGameJam
import software.setixx.chimu.domain.model.GameJam
import software.setixx.chimu.domain.model.GameJamDetails
import software.setixx.chimu.domain.model.UpdateGameJam

interface GameJamRepository {
    val jams: Flow<List<GameJam>>
    suspend fun getAllJams(): ApiResult<List<GameJam>>
    suspend fun createJam(data: CreateGameJam): ApiResult<GameJamDetails>
    suspend fun cancelJam(gameJamId: String): ApiResult<GameJamDetails>
    suspend fun getJamDetails(gameJamId: String): ApiResult<GameJamDetails>
    suspend fun deleteJam(gameJamId: String): ApiResult<Unit>
    suspend fun updateJam(gameJamId: String, data: UpdateGameJam): ApiResult<GameJamDetails>
}