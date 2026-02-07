package software.setixx.chimu.domain.repository

import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.model.CreateProject
import software.setixx.chimu.domain.model.Project
import software.setixx.chimu.domain.model.ProjectDetails
import software.setixx.chimu.domain.model.ProjectStatus
import software.setixx.chimu.domain.model.UpdateProject

// TODO апи доделать под загрузку файлов
interface ProjectRepository {
    suspend fun submitProject(projectId: String): ApiResult<ProjectDetails>
    suspend fun returnDraft(projectId: String): ApiResult<ProjectDetails>
    suspend fun publishProject(projectId: String): ApiResult<ProjectDetails>
    suspend fun getProjectFiles(projectId: String): ApiResult<Unit>
    suspend fun uploadProjectFiles(projectId: String): ApiResult<Unit>
    suspend fun disqualifyProject(projectId: String): ApiResult<ProjectDetails>
    suspend fun getJamProjects(jamId: String, status: ProjectStatus): ApiResult<List<Project>>
    suspend fun createProject(jamId: String, data: CreateProject): ApiResult<ProjectDetails>
    suspend fun getProject(projectId: String): ApiResult<ProjectDetails>
    suspend fun deleteProject(projectId: String): ApiResult<Unit>
    suspend fun updateProject(projectId: String, data: UpdateProject): ApiResult<ProjectDetails>
    suspend fun getUserProjects(): ApiResult<List<Project>>
    suspend fun getTeamProjects(teamId: String): ApiResult<List<Project>>
}