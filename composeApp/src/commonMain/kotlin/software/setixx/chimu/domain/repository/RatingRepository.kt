package software.setixx.chimu.domain.repository

import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.model.JudgeProgress
import software.setixx.chimu.domain.model.MyRating
import software.setixx.chimu.domain.model.ProjectRating
import software.setixx.chimu.domain.model.RateProject
import software.setixx.chimu.domain.model.Rating
import software.setixx.chimu.domain.model.UpdateRating

interface RatingRepository {
    suspend fun getProjectRatings(projectId: String): ApiResult<ProjectRating>
    suspend fun rateProject(projectId: String, data: RateProject): ApiResult<Rating>
    suspend fun deleteProjectRating(ratingId: String): ApiResult<Unit>
    suspend fun updateProjectRating(ratingId: String, data: UpdateRating): ApiResult<Rating>
    suspend fun getMyRatings(projectId: String): ApiResult<List<MyRating>>
    suspend fun getJudgeProgress(jamId: String): ApiResult<JudgeProgress>
}