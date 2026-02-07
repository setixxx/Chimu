package software.setixx.chimu.domain.repository

import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.model.CreateRatingCriteria
import software.setixx.chimu.domain.model.RatingCriteria
import software.setixx.chimu.domain.model.UpdateRatingCriteria

interface RatingCriteriaRepository {
    suspend fun getJamCriteria(jamId: String): ApiResult<List<RatingCriteria>>
    suspend fun createJamCriteria(jamId: String, data: CreateRatingCriteria): ApiResult<RatingCriteria>
    suspend fun deleteJamCriteria(jamId: String, criteriaId: Long): ApiResult<Unit>
    suspend fun updateJamCriteria(jamId: String, criteriaId: Long, data: UpdateRatingCriteria): ApiResult<RatingCriteria>
}