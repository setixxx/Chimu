package software.setixx.chimu.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import software.setixx.chimu.api.domain.ProjectStatus
import software.setixx.chimu.data.local.TokenStorage
import software.setixx.chimu.data.remote.ProjectApi
import software.setixx.chimu.data.remote.dto.CreateProjectRequest
import software.setixx.chimu.data.remote.dto.ProjectDetailsResponse
import software.setixx.chimu.data.remote.dto.ProjectResponse
import software.setixx.chimu.data.remote.dto.UpdateProjectRequest
import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.model.CreateProject
import software.setixx.chimu.domain.model.Project
import software.setixx.chimu.domain.model.ProjectDetails
import software.setixx.chimu.domain.model.UpdateProject
import software.setixx.chimu.domain.repository.ProjectRepository

class ProjectRepositoryImpl(
    private val api: ProjectApi,
    private val tokenStorage: TokenStorage
) : ProjectRepository {
    private val _userProjects = MutableStateFlow<List<Project>>(emptyList())
    override val userProjects: Flow<List<Project>> = _userProjects

    private val _jamProjects = MutableStateFlow<List<Project>>(emptyList())
    override val jamProjects: Flow<List<Project>> = _jamProjects

    private val _teamProjects = MutableStateFlow<List<Project>>(emptyList())
    override val teamProjects: Flow<List<Project>> = _teamProjects

    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    override val projects: Flow<List<Project>> = _projects

    override suspend fun getUserProjects(): ApiResult<List<Project>> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")

            val response = api.getUserProjects(token)
            val projects = response.map { it.toDomain() }
            _userProjects.value = projects
            ApiResult.Success(projects)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        } catch (e: IllegalArgumentException) {
            ApiResult.Error(e.message ?: "Неизвестная ошибка")
        }
    }

    override suspend fun getJamProjects(
        jamId: String,
        status: ProjectStatus
    ): ApiResult<List<Project>> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")
            val response = api.getJamProjects(token, jamId, status)
            val projects = response.map { it.toDomain() }
            _jamProjects.value = projects
            ApiResult.Success(projects)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        } catch (e: IllegalArgumentException) {
            ApiResult.Error(e.message ?: "Неизвестная ошибка")
        }
    }

    override suspend fun getTeamProjects(teamId: String): ApiResult<List<Project>> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")
            val response = api.getTeamProjects(token, teamId)
            val projects = response.map { it.toDomain() }
            _teamProjects.value = projects
            ApiResult.Success(projects)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        } catch (e: IllegalArgumentException) {
            ApiResult.Error(e.message ?: "Неизвестная ошибка")
        }
    }

    override suspend fun submitProject(projectId: String): ApiResult<ProjectDetails> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")
            val response = api.submitProject(token, projectId)
            ApiResult.Success(response.toDomain())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    override suspend fun returnDraft(projectId: String): ApiResult<ProjectDetails> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")
            val response = api.returnDraft(token, projectId)
            ApiResult.Success(response.toDomain())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    override suspend fun publishProject(projectId: String): ApiResult<ProjectDetails> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")
            val response = api.publishProject(token, projectId)
            ApiResult.Success(response.toDomain())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    override suspend fun disqualifyProject(projectId: String): ApiResult<ProjectDetails> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")
            val response = api.disqualifyProject(token, projectId)
            ApiResult.Success(response.toDomain())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    override suspend fun createProject(
        jamId: String,
        data: CreateProject,
    ): ApiResult<ProjectDetails> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")
            val request = CreateProjectRequest(
                title = data.title,
                description = data.description,
                gameUrl = data.gameUrl
            )
            val response = api.createProject(token, jamId, request)
            ApiResult.Success(response.toDomain())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    override suspend fun getProject(projectId: String): ApiResult<ProjectDetails> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")
            val response = api.getProject(token, projectId)
            ApiResult.Success(response.toDomain())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    override suspend fun deleteProject(projectId: String): ApiResult<Unit> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")
            api.deleteProject(token, projectId)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    override suspend fun updateProject(
        projectId: String,
        data: UpdateProject,
    ): ApiResult<ProjectDetails> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")
            val request = UpdateProjectRequest(
                title = data.title,
                description = data.description,
                gameUrl = data.gameUrl
            )
            val response = api.updateProject(token, projectId, request)
            ApiResult.Success(response.toDomain())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    private fun ProjectDetailsResponse.toDomain(): ProjectDetails {
        return ProjectDetails(
            id = id,
            jamId = jamId,
            jamName = jamName,
            teamId = teamId,
            teamName = teamName,
            title = title,
            description = description,
            gameUrl = gameUrl,
            status = status,
            submittedAt = submittedAt,
            createdAt = createdAt,
            updatedAt = updatedAt,
            canEdit = canEdit,
            canSubmit = canSubmit,
            canDelete = canDelete
        )
    }

    private fun ProjectResponse.toDomain(): Project {
        return Project(
            id = id,
            jamId = jamId,
            jamName = jamName,
            teamId = teamId,
            teamName = teamName,
            title = title,
            description = description,
            gameUrl = gameUrl,
            status = ProjectStatus.valueOf(status),
            submittedAt = submittedAt,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
