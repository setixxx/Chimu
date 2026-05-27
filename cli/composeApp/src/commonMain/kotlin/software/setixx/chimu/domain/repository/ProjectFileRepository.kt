package software.setixx.chimu.domain.repository

import software.setixx.chimu.api.domain.ProjectFileType
import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.model.ProjectFile

interface ProjectFileRepository {
    suspend fun getProjectFiles(projectId: String): ApiResult<List<ProjectFile>>
    suspend fun uploadProjectFile(projectId: String, file: ProjectFile): ApiResult<ProjectFile>
    suspend fun downloadProjectFile(projectId: String, fileId: String, fileType: ProjectFileType): ApiResult<ByteArray>
    suspend fun deleteProjectFile(projectId: String, fileId: String, fileType: ProjectFileType): ApiResult<Unit>
}