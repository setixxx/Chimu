package software.setixx.chimu.domain.usecase

import software.setixx.chimu.domain.model.ProjectFile
import software.setixx.chimu.domain.repository.ProjectFileRepository

class GetProjectFilesUseCase(private val repository: ProjectFileRepository) {
    suspend operator fun invoke(projectId: String) =
        repository.getProjectFiles(projectId)
}

class UploadProjectFileUseCase(private val repository: ProjectFileRepository) {
    suspend operator fun invoke(projectId: String, file: ProjectFile) =
        repository.uploadProjectFile(projectId, file)
}

class DownloadProjectFileUseCase(private val repository: ProjectFileRepository) {
    suspend operator fun invoke(projectId: String, fileId: String) =
        repository.downloadProjectFile(projectId, fileId)
}

class DeleteProjectFileUseCase(private val repository: ProjectFileRepository) {
    suspend operator fun invoke(projectId: String, fileId: String) =
        repository.deleteProjectFile(projectId, fileId)
}
