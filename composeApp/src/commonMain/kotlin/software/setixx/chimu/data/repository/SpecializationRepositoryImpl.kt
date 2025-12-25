package software.setixx.chimu.data.repository

import software.setixx.chimu.data.remote.SpecializationApi
import software.setixx.chimu.domain.model.Specialization
import software.setixx.chimu.domain.repository.SpecializationRepository

class SpecializationRepositoryImpl(
    private val api: SpecializationApi
) : SpecializationRepository {

    override suspend fun getAllSpecializations(): Result<List<Specialization>> {
        return try {
            val response = api.getAllSpecializations()
            val specializations = response.map { dto ->
                Specialization(
                    id = dto.id,
                    name = dto.name,
                    description = dto.description
                )
            }
            Result.success(specializations)
        } catch (e: Exception) {
            println("❌ Error loading specializations: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
}