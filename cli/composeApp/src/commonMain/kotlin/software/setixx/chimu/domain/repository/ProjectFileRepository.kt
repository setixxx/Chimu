package software.setixx.chimu.domain.repository

import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.model.ProjectFile

interface ProjectFileRepository {
    suspend fun getProjectFiles(projectId: String): ApiResult<List<ProjectFile>>
    suspend fun uploadProjectFile(projectId: String, file: ProjectFile): ApiResult<ProjectFile>
    suspend fun downloadProjectFile(projectId: String, fileId: String): ApiResult<ProjectFile>
    suspend fun deleteProjectFile(projectId: String, fileId: String): ApiResult<Unit>
}