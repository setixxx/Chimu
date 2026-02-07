package software.setixx.chimu.domain.usecase

import software.setixx.chimu.domain.model.CreateRatingCriteria
import software.setixx.chimu.domain.model.UpdateRatingCriteria
import software.setixx.chimu.domain.repository.RatingCriteriaRepository

class GetJamCriteriaUseCase(
    private val repository: RatingCriteriaRepository
) {
    suspend operator fun invoke(jamId: String) = repository.getJamCriteria(jamId)
}

class CreateJamCriteriaUseCase(
    private val repository: RatingCriteriaRepository
) {
    suspend operator fun invoke(jamId: String, data: CreateRatingCriteria) = repository.createJamCriteria(jamId, data)
}

class DeleteJamCriteriaUseCase(
    private val repository: RatingCriteriaRepository
) {
    suspend operator fun invoke(jamId: String, criteriaId: Long) = repository.deleteJamCriteria(jamId, criteriaId)
}

class UpdateJamCriteriaUseCase(
    private val repository: RatingCriteriaRepository
) {
    suspend operator fun invoke(jamId: String, criteriaId: Long, data: UpdateRatingCriteria) =
        repository.updateJamCriteria(jamId, criteriaId, data)
}