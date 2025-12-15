package setixx.software.Chimu.service

import org.springframework.stereotype.Service
import setixx.software.Chimu.domain.Specialization
import setixx.software.Chimu.dto.SpecializationResponse
import setixx.software.Chimu.repository.SpecializationRepository

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