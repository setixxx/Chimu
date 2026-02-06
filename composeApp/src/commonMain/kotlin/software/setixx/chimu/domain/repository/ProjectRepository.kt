package software.setixx.chimu.domain.repository

import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.model.Project

interface ProjectRepository {
    suspend fun getUserProjects(): ApiResult<List<Project>>
}