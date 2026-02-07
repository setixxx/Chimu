package software.setixx.chimu.data.repository

import software.setixx.chimu.data.local.TokenStorage
import software.setixx.chimu.data.remote.RatingApi
import software.setixx.chimu.data.remote.dto.*
import software.setixx.chimu.domain.model.*
import software.setixx.chimu.domain.repository.RatingRepository

class RatingRepositoryImpl(
    private val api: RatingApi,
    private val tokenStorage: TokenStorage
) : RatingRepository {

    override suspend fun getProjectRatings(projectId: String): ApiResult<ProjectRating> {
        return try {
            val token = tokenStorage.getAccessToken() ?: return ApiResult.Error("Ошибка аутентификации")
            val response = api.getProjectRatings(projectId, token)
            ApiResult.Success(response.toDomain())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    override suspend fun rateProject(projectId: String, data: RateProject): ApiResult<Rating> {
        return try {
            val token = tokenStorage.getAccessToken() ?: return ApiResult.Error("Ошибка аутентификации")
            val request = RateProjectRequest(
                criteriaId = data.criteriaId,
                score = data.score,
                comment = data.comment
            )
            val response = api.rateProject(projectId, request, token)
            ApiResult.Success(response.toDomain())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    override suspend fun deleteProjectRating(ratingId: String): ApiResult<Unit> {
        return try {
            val token = tokenStorage.getAccessToken() ?: return ApiResult.Error("Ошибка аутентификации")
            api.deleteProjectRating(ratingId, token)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    override suspend fun updateProjectRating(ratingId: String, data: UpdateRating): ApiResult<Rating> {
        return try {
            val token = tokenStorage.getAccessToken() ?: return ApiResult.Error("Ошибка аутентификации")
            val request = UpdateRatingRequest(
                score = data.score,
                comment = data.comment
            )
            val response = api.updateProjectRating(ratingId, request, token)
            ApiResult.Success(response.toDomain())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    override suspend fun getMyRatings(projectId: String): ApiResult<List<MyRating>> {
        return try {
            val token = tokenStorage.getAccessToken() ?: return ApiResult.Error("Ошибка аутентификации")
            val response = api.getMyRatings(projectId, token)
            ApiResult.Success(response.map { it.toDomain() })
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    override suspend fun getJudgeProgress(jamId: String): ApiResult<JudgeProgress> {
        return try {
            val token = tokenStorage.getAccessToken() ?: return ApiResult.Error("Ошибка аутентификации")
            val response = api.getJudgeProgress(jamId, token)
            ApiResult.Success(response.toDomain())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    private fun ProjectRatingResponse.toDomain() = ProjectRating(
        projectId = projectId,
        criteriaRatings = criteriaRatings.map { criteria ->
            ProjectCriteriaRating(
                criteriaId = criteria.criteriaId,
                criteriaName = criteria.criteriaName,
                maxScore = criteria.maxScore,
                weight = criteria.weight,
                averageScore = criteria.averageScore,
                judgeRatings = criteria.judgeRatings.map { judge ->
                    ProjectJudgeRating(
                        judgeNickname = judge.judgeNickname,
                        score = judge.score,
                        comment = judge.comment
                    )
                }
            )
        }
    )

    private fun RatingResponse.toDomain() = Rating(
        id = id,
        projectId = projectId,
        judgeId = judgeId,
        judgeNickname = judgeNickname,
        criteriaId = criteriaId,
        criteriaName = criteriaName,
        score = score,
        maxScore = maxScore,
        comment = comment,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun MyRatingResponse.toDomain() = MyRating(
        id = id,
        criteriaId = criteriaId,
        criteriaName = criteriaName,
        score = score,
        maxScore = maxScore,
        comment = comment,
        updatedAt = updatedAt
    )

    private fun JudgeProgressResponse.toDomain() = JudgeProgress(
        jamId = jamId,
        jamName = jamName,
        totalProjects = totalProjects,
        ratedProjects = ratedProjects,
        missingProjects = missingProjects.map { missing ->
            software.setixx.chimu.domain.model.MissingProjectInfo(
                projectId = missing.projectId,
                projectTitle = missing.projectTitle,
                teamName = missing.teamName,
                missingCriteria = missing.missingCriteria
            )
        }
    )
}
