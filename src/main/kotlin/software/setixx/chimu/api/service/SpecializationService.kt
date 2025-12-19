package software.setixx.chimu.api.service

import org.springframework.stereotype.Service
import software.setixx.chimu.api.domain.Specialization
import software.setixx.chimu.api.dto.SpecializationResponse
import software.setixx.chimu.api.repository.SpecializationRepository

@Service
class SpecializationService(
    private val specializationRepository: SpecializationRepository
) {
    fun getAllSpecializations(): List<SpecializationResponse> {
        return specializationRepository.findAll().map { toResponse(it) }
    }

    fun getSpecializationById(id: Long): Specialization {
        return specializationRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Specialization not found") }
    }

    private fun toResponse(specialization: Specialization): SpecializationResponse {
        return SpecializationResponse(
            id = specialization.id!!,
            name = specialization.name,
            description = specialization.description
        )
    }
}