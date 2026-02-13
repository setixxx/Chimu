package software.setixx.chimu.data.repository

import software.setixx.chimu.data.local.TokenStorage
import software.setixx.chimu.data.remote.LeaderboardApi
import software.setixx.chimu.data.remote.dto.*
import software.setixx.chimu.domain.model.*
import software.setixx.chimu.domain.repository.LeaderboardRepository

class LeaderboardRepositoryImpl(
    private val api: LeaderboardApi,
    private val tokenStorage: TokenStorage
) : LeaderboardRepository {

    override suspend fun getJamStatistics(jamId: String): ApiResult<JamStatistics> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")

            val response = api.getJamStatistics(jamId, token)
            ApiResult.Success(response.toDomain())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    override suspend fun getLeaderboard(jamId: String): ApiResult<Leaderboard> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")

            val response = api.getLeaderboard(jamId, token)
            ApiResult.Success(response.toDomain())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    private fun JamStatisticsResponse.toDomain(): JamStatistics {
        return JamStatistics(
            jamId = jamId,
            jamName = jamName,
            totalProjects = totalProjects,
            publishedProjects = publishedProjects,
            disqualifiedProjects = disqualifiedProjects,
            totalJudges = totalJudges,
            averageScoresPerCriteria = averageScoresPerCriteria.map { 
                software.setixx.chimu.domain.model.CriteriaAverageScore(
                    criteriaName = it.criteriaName,
                    averageScore = it.averageScore,
                    maxScore = it.maxScore
                )
            },
            judgeCompletionRate = judgeCompletionRate.map { 
                software.setixx.chimu.domain.model.JudgeCompletion(
                    judgeNickname = it.judgeNickname,
                    ratedProjects = it.ratedProjects,
                    totalProjects = it.totalProjects,
                    completionPercentage = it.completionPercentage
                )
            }
        )
    }

    private fun LeaderboardResponse.toDomain(): Leaderboard {
        return Leaderboard(
            jamId = jamId,
            jamName = jamName,
            jamStatus = jamStatus,
            totalProjects = totalProjects,
            qualifiedProjects = qualifiedProjects,
            totalJudges = totalJudges,
            rankings = rankings.map { it.toDomain() }
        )
    }

    private fun ProjectRankingResponse.toDomain(): ProjectRanking {
        return ProjectRanking(
            rank = rank,
            project = software.setixx.chimu.domain.model.ProjectInfo(
                id = project.id,
                title = project.title,
                description = project.description,
                teamId = project.teamId,
                teamName = project.teamName,
                gameUrl = project.gameUrl,
                repositoryUrl = project.repositoryUrl,
                submittedAt = project.submittedAt
            ),
            score = software.setixx.chimu.domain.model.ScoreBreakdown(
                total = score.total,
                breakdown = score.breakdown.map { 
                    software.setixx.chimu.domain.model.CriteriaScoreDetail(
                        criteriaId = it.criteriaId,
                        criteriaName = it.criteriaName,
                        weight = it.weight,
                        maxScore = it.maxScore,
                        averageScore = it.averageScore,
                        weightedScore = it.weightedScore,
                        judgeCount = it.judgeCount,
                        scores = it.scores
                    )
                },
                allCriteriaRated = score.allCriteriaRated,
                judgesRated = score.judgesRated,
                totalJudges = score.totalJudges
            )
        )
    }
}
