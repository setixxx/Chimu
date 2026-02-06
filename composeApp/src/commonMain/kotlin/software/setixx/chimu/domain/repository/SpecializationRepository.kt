package software.setixx.chimu.domain.repository

import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.model.Specialization

interface SpecializationRepository {
    suspend fun getAllSpecializations(): ApiResult<List<Specialization>>
}