package software.setixx.chimu.domain.repository

import kotlinx.coroutines.flow.Flow
import software.setixx.chimu.api.domain.ProjectStatus
import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.model.CreateProject
import software.setixx.chimu.domain.model.Project
import software.setixx.chimu.domain.model.ProjectDetails
import software.setixx.chimu.domain.model.UpdateProject

interface ProjectRepository {
    val userProjects: Flow<List<Project>>
    val jamProjects: Flow<List<Project>>
    val teamProjects: Flow<List<Project>>
    val projects: Flow<List<Project>>
    suspend fun submitProject(projectId: String): ApiResult<ProjectDetails>
    suspend fun returnDraft(projectId: String): ApiResult<ProjectDetails>
    suspend fun publishProject(projectId: String): ApiResult<ProjectDetails>
    suspend fun disqualifyProject(projectId: String): ApiResult<ProjectDetails>
    suspend fun getJamProjects(jamId: String, status: ProjectStatus): ApiResult<List<Project>>
    suspend fun createProject(jamId: String, data: CreateProject): ApiResult<ProjectDetails>
    suspend fun getProject(projectId: String): ApiResult<ProjectDetails>
    suspend fun deleteProject(projectId: String): ApiResult<Unit>
    suspend fun updateProject(projectId: String, data: UpdateProject): ApiResult<ProjectDetails>
    suspend fun getUserProjects(): ApiResult<List<Project>>
    suspend fun getTeamProjects(teamId: String): ApiResult<List<Project>>
}