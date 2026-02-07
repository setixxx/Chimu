package software.setixx.chimu.domain.usecase

import software.setixx.chimu.domain.model.RateProject
import software.setixx.chimu.domain.model.UpdateRating
import software.setixx.chimu.domain.repository.RatingRepository

class GetProjectRatingsUseCases(
    private val repository: RatingRepository
){
    suspend operator fun invoke(projectId: String) = repository.getProjectRatings(projectId)
}

class RateProjectUseCase(
    private val repository: RatingRepository
) {
    suspend operator fun invoke(
        projectId: String, data: RateProject
    ) = repository.rateProject(projectId, data)
}

class DeleteProjectRatingUseCase(
    private val repository: RatingRepository
){
    suspend operator fun invoke(projectId: String) = repository.deleteProjectRating(projectId)
}

class UpdateProjectRatingUseCase(
    private val repository: RatingRepository
){
    suspend operator fun invoke(projectId: String, data: UpdateRating)
    = repository.updateProjectRating(projectId, data)
}

class GetMyRatingsUseCase(
    private val repository: RatingRepository
){
    suspend operator fun invoke(projectId: String) = repository.getMyRatings(projectId)
}

class GetJudgeProgressUseCase(
    private val repository: RatingRepository
){
    suspend operator fun invoke(jamId: String) = repository.getJudgeProgress(jamId)
}