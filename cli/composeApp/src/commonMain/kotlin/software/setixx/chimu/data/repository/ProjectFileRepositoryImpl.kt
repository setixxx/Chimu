package software.setixx.chimu.data.repository

import software.setixx.chimu.api.domain.ProjectFileType
import software.setixx.chimu.data.local.TokenStorage
import software.setixx.chimu.data.remote.ProjectFileApi
import software.setixx.chimu.data.remote.dto.ProjectFileResponse
import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.model.FileUpload
import software.setixx.chimu.domain.model.ProjectFile
import software.setixx.chimu.domain.repository.ProjectFileRepository

class ProjectFileRepositoryImpl(
    private val api: ProjectFileApi,
    private val tokenStorage: TokenStorage
) : ProjectFileRepository {

    override suspend fun getProjectFiles(projectId: String): ApiResult<List<ProjectFile>> {
        return try {
            val token = tokenStorage.getAccessToken() ?: return ApiResult.Error("Ошибка авторизации")
            val response = api.getFiles(projectId, token)
            ApiResult.Success(response.map { it.toDomain() })
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Неизвестная ошибка")
        }
    }

    override suspend fun uploadProjectFile(projectId: String, file: ProjectFile): ApiResult<ProjectFile> {
        return try {
            val token = tokenStorage.getAccessToken() ?: return ApiResult.Error("Ошибка авторизации")
            val fileUpload = FileUpload(
                fileName = file.fileName,
                bytes = file.bytes ?: throw IllegalArgumentException("Данные файла отсутствуют"),
                mimeType = file.mimeType,
                fileType = file.fileType
            )
            val response = when (file.fileType) {
                ProjectFileType.SCREENSHOT -> api.uploadScreenshot(projectId, token, fileUpload)
                ProjectFileType.VIDEO -> api.uploadVideo(projectId, token, fileUpload)
                else -> api.uploadFile(projectId, token, fileUpload)
            }
            ApiResult.Success(response.toDomain())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Неизвестная ошибка")
        }
    }

    override suspend fun downloadProjectFile(
        projectId: String,
        fileId: String,
        fileType: ProjectFileType
    ): ApiResult<ByteArray> {
        return try {
            val token = tokenStorage.getAccessToken() ?: return ApiResult.Error("Ошибка авторизации")
            val bytes = when (fileType) {
                ProjectFileType.SCREENSHOT -> api.downloadScreenshot(projectId, fileId, token)
                ProjectFileType.VIDEO -> api.downloadVideo(projectId, fileId, token)
                else -> api.downloadFile(projectId, fileId, token)
            }
            ApiResult.Success(bytes)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Неизвестная ошибка")
        }
    }

    override suspend fun deleteProjectFile(
        projectId: String,
        fileId: String,
        fileType: ProjectFileType
    ): ApiResult<Unit> {
        return try {
            val token = tokenStorage.getAccessToken() ?: return ApiResult.Error("Ошибка авторизации")
            when (fileType) {
                ProjectFileType.SCREENSHOT -> api.deleteScreenshot(projectId, fileId, token)
                ProjectFileType.VIDEO -> api.deleteVideo(projectId, fileId, token)
                else -> api.deleteFile(projectId, fileId, token)
            }
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Неизвестная ошибка")
        }
    }

    private fun ProjectFileResponse.toDomain(): ProjectFile = ProjectFile(
        id = id,
        fileName = fileName,
        fileSize = fileSize,
        mimeType = mimeType,
        fileType = fileType,
        uploadedAt = uploadedAt,
        uploadedByUserId = uploadedByUserId
    )
}