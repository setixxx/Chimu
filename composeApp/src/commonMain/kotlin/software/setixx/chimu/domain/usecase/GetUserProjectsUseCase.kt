package software.setixx.chimu.domain.usecase

import software.setixx.chimu.domain.model.Project
import software.setixx.chimu.domain.repository.ProjectRepository

class GetUserProjectsUseCase(
    private val repository: ProjectRepository
) {
    suspend operator fun invoke() = repository.getUserProjects()
}