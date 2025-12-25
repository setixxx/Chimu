package software.setixx.chimu.domain.repository

import software.setixx.chimu.domain.model.Specialization

interface SpecializationRepository {
    suspend fun getAllSpecializations(): Result<List<Specialization>>
}