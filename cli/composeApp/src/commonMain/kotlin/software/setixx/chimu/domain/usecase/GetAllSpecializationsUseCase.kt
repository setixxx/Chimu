package software.setixx.chimu.domain.usecase

import software.setixx.chimu.domain.model.Specialization
import software.setixx.chimu.domain.repository.SpecializationRepository

class GetAllSpecializationsUseCase(
    private val repository: SpecializationRepository
) {
    suspend operator fun invoke() = repository.getAllSpecializations()
}