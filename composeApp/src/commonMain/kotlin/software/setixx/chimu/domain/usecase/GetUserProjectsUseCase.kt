package software.setixx.chimu.domain.usecase

import software.setixx.chimu.domain.model.CreateProject
import software.setixx.chimu.domain.model.Project
import software.setixx.chimu.domain.model.ProjectStatus
import software.setixx.chimu.domain.model.UpdateProject
import software.setixx.chimu.domain.repository.ProjectRepository

class SubmitProjectUseCase(
    private val repository: ProjectRepository
) {
    suspend operator fun invoke(projectId: String) = repository.submitProject(projectId)
}

class ReturnDraftUseCase(
    private val repository: ProjectRepository
) {
    suspend operator fun invoke(projectId: String) = repository.returnDraft(projectId)
}

class PublishProjectUseCase(
    private val repository: ProjectRepository
) {
    suspend operator fun invoke(projectId: String) = repository.publishProject(projectId)
}

class GetProjectFilesUseCase(
    private val repository: ProjectRepository
) {
    suspend operator fun invoke(projectId: String) = repository.getProjectFiles(projectId)
}

class UploadProjectFilesUseCase(
    private val repository: ProjectRepository
) {
    suspend operator fun invoke(projectId: String) = repository.uploadProjectFiles(projectId)
}

class DisqualifyProjectUseCase(
    private val repository: ProjectRepository
) {
    suspend operator fun invoke(projectId: String) = repository.disqualifyProject(projectId)
}

class GetJamProjectsUseCase(
    private val repository: ProjectRepository
) {
    suspend operator fun invoke(jamId: String, status: ProjectStatus) =
        repository.getJamProjects(jamId, status)
}

class CreateProjectUseCase(
    private val repository: ProjectRepository
) {
    suspend operator fun invoke(jamId: String, data: CreateProject) =
        repository.createProject(jamId, data)
}

class GetProjectUseCase(
    private val repository: ProjectRepository
) {
    suspend operator fun invoke(projectId: String) = repository.getProject(projectId)
}

class DeleteProjectUseCase(
    private val repository: ProjectRepository
) {
    suspend operator fun invoke(projectId: String) = repository.deleteProject(projectId)
}

class UpdateProjectUseCase(
    private val repository: ProjectRepository
){
    suspend operator fun invoke(projectId: String, data: UpdateProject) = repository.updateProject(projectId, data)
}

class GetUserProjectsUseCase(
    private val repository: ProjectRepository
) {
    suspend operator fun invoke() = repository.getUserProjects()
}

class GetTeamProjectsUseCase(
    private val repository: ProjectRepository
) {
    suspend operator fun invoke(teamId: String) = repository.getTeamProjects(teamId)
}