package software.setixx.chimu.domain.repository

import software.setixx.chimu.domain.model.Project

interface ProjectRepository {
    suspend fun getUserProjects(): Result<List<Project>>
}