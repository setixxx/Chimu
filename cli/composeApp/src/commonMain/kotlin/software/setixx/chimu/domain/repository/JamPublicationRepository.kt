package software.setixx.chimu.domain.repository

import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.model.FileUpload
import software.setixx.chimu.domain.model.GameJamDetails
import software.setixx.chimu.domain.model.JamBanner

interface JamPublicationRepository {
    suspend fun publishJam(jamId: String): ApiResult<GameJamDetails>
    suspend fun getBanner(jamId: String): ApiResult<JamBanner>
    suspend fun uploadBanner(jamId: String, file: FileUpload): ApiResult<Unit>
    suspend fun deleteBanner(jamId: String): ApiResult<Unit>
}
