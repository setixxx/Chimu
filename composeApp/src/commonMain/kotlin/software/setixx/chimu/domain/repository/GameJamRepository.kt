package software.setixx.chimu.domain.repository

import software.setixx.chimu.domain.model.GameJam

interface GameJamRepository {
    suspend fun getAllJams(): Result<List<GameJam>>
    suspend fun getActiveJams(): Result<List<GameJam>>
}