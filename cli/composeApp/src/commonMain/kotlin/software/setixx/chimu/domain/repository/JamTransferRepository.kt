package software.setixx.chimu.domain.repository

import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.model.CreateJamTransfer
import software.setixx.chimu.domain.model.JamTransfer
import software.setixx.chimu.domain.model.ReviewJamTransfer

interface JamTransferRepository {
    suspend fun createTransferRequest(jamId: String, data: CreateJamTransfer): ApiResult<JamTransfer>
    suspend fun cancelTransferRequest(jamId: String): ApiResult<JamTransfer>
    suspend fun reviewTransferRequest(requestId: String, data: ReviewJamTransfer): ApiResult<JamTransfer>
    suspend fun getTransferRequests(): ApiResult<List<JamTransfer>>
}