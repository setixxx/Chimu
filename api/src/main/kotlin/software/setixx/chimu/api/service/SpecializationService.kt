package software.setixx.chimu.api.service

import org.springframework.stereotype.Service
import software.setixx.chimu.api.domain.Specialization
import software.setixx.chimu.api.dto.SpecializationResponse
import software.setixx.chimu.api.repository.SpecializationRepository
import java.util.UUID

@Service
class SpecializationService(
    private val specializationRepository: SpecializationRepository
) {
    fun getAllSpecializations(): List<SpecializationResponse> {
        return specializationRepository.findAll().map { toResponse(it) }
    }

    fun getSpecializationByPublicId(publicId: String): Specialization {
        return specializationRepository.findByPublicId(UUID.fromString(publicId))
            ?: throw IllegalArgumentException("Specialization not found")
    }

    fun getSpecializationById(id: Long): Specialization {
        return specializationRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Specialization not found") }
    }

    private fun toResponse(specialization: Specialization): SpecializationResponse {
        return SpecializationResponse(
            id = specialization.publicId.toString(),
            name = specialization.name,
            description = specialization.description
        )
    }
}