package software.setixx.chimu.data.repository

import software.setixx.chimu.data.local.TokenStorage
import software.setixx.chimu.data.remote.ProjectApi
import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.model.Project
import software.setixx.chimu.domain.model.ProjectStatus
import software.setixx.chimu.domain.repository.ProjectRepository

class ProjectRepositoryImpl(
    private val api: ProjectApi,
    private val tokenStorage: TokenStorage
) : ProjectRepository {

    override suspend fun getUserProjects(): ApiResult<List<Project>> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")

            val response = api.getUserProjects(token)
            val projects = response.map { dto ->
                Project(
                    id = dto.id,
                    jamName = dto.jamName,
                    teamName = dto.teamName,
                    title = dto.title,
                    status = ProjectStatus.valueOf(dto.status),
                    submittedAt = dto.submittedAt
                )
            }
            ApiResult.Success(projects)
        } catch (e: Exception) {
            println("Error loading projects: ${e.message}")
            e.printStackTrace()
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }
}