package software.setixx.chimu.data.repository

import software.setixx.chimu.data.local.TokenStorage
import software.setixx.chimu.data.remote.ProjectApi
import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.model.CreateProject
import software.setixx.chimu.domain.model.Project
import software.setixx.chimu.domain.model.ProjectDetails
import software.setixx.chimu.domain.model.ProjectStatus
import software.setixx.chimu.domain.model.UpdateProject
import software.setixx.chimu.domain.repository.ProjectRepository

class ProjectRepositoryImpl(
    private val api: ProjectApi,
    private val tokenStorage: TokenStorage
) : ProjectRepository {
    override suspend fun submitProject(projectId: String): ApiResult<ProjectDetails> {
        TODO("Not yet implemented")
    }

    override suspend fun returnDraft(projectId: String): ApiResult<ProjectDetails> {
        TODO("Not yet implemented")
    }

    override suspend fun publishProject(projectId: String): ApiResult<ProjectDetails> {
        TODO("Not yet implemented")
    }

    override suspend fun getProjectFiles(projectId: String): ApiResult<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun uploadProjectFiles(projectId: String): ApiResult<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun disqualifyProject(projectId: String): ApiResult<ProjectDetails> {
        TODO("Not yet implemented")
    }

    override suspend fun getJamProjects(
        jamId: String,
        status: ProjectStatus,
    ): ApiResult<List<Project>> {
        TODO("Not yet implemented")
    }

    override suspend fun createProject(
        jamId: String,
        data: CreateProject,
    ): ApiResult<ProjectDetails> {
        TODO("Not yet implemented")
    }

    override suspend fun getProject(projectId: String): ApiResult<ProjectDetails> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteProject(projectId: String): ApiResult<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun updateProject(
        projectId: String,
        data: UpdateProject,
    ): ApiResult<ProjectDetails> {
        TODO("Not yet implemented")
    }

    override suspend fun getUserProjects(): ApiResult<List<Project>> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")

            val response = api.getUserProjects(token)
            val projects = response.map { dto ->
                Project(
                    id = dto.id,
                    jamId = dto.jamId,
                    jamName = dto.jamName,
                    teamId = dto.teamId,
                    teamName = dto.teamName,
                    title = dto.title,
                    description = dto.description,
                    gameUrl = dto.gameUrl,
                    repositoryUrl = dto.repositoryUrl,
                    status = ProjectStatus.valueOf(dto.status),
                    submittedAt = dto.submittedAt,
                    createdAt = dto.createdAt,
                    updatedAt = dto.updatedAt
                )
            }
            ApiResult.Success(projects)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        } catch (e: IllegalArgumentException) {
            ApiResult.Error(e.message ?: "Неизвестная ошибка")
        }
    }

    override suspend fun getTeamProjects(teamId: String): ApiResult<List<Project>> {
        TODO("Not yet implemented")
    }
}
