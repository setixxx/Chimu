package software.setixx.chimu.presentation.jam.details.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import software.setixx.chimu.api.domain.ProjectStatus
import software.setixx.chimu.api.domain.RegistrationStatus
import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.model.CreateProject
import software.setixx.chimu.domain.model.FileUpload
import software.setixx.chimu.domain.model.ProjectFile
import software.setixx.chimu.domain.model.UpdateRegistrationStatus
import software.setixx.chimu.domain.usecase.CreateProjectUseCase
import software.setixx.chimu.domain.usecase.DeleteProjectUseCase
import software.setixx.chimu.domain.usecase.DeleteProjectFileUseCase
import software.setixx.chimu.domain.usecase.DisqualifyProjectUseCase
import software.setixx.chimu.domain.usecase.GetJamStatisticsUseCase
import software.setixx.chimu.domain.usecase.GetJamProjectsUseCase
import software.setixx.chimu.domain.usecase.GetJamRegistrationsUseCase
import software.setixx.chimu.domain.usecase.GetProjectFilesUseCase
import software.setixx.chimu.domain.usecase.GetProjectUseCase
import software.setixx.chimu.domain.usecase.GetTeamProjectsUseCase
import software.setixx.chimu.domain.usecase.GetUserTeamsUseCase
import software.setixx.chimu.domain.usecase.ReturnDraftUseCase
import software.setixx.chimu.domain.usecase.SubmitProjectUseCase
import software.setixx.chimu.domain.usecase.UpdateRegistrationStatusUseCase
import software.setixx.chimu.domain.usecase.UploadProjectFileUseCase

class ProgressViewModel(
    private val getUserTeamsUseCase: GetUserTeamsUseCase,
    private val getJamRegistrationsUseCase: GetJamRegistrationsUseCase,
    private val getTeamProjectsUseCase: GetTeamProjectsUseCase,
    private val getProjectUseCase: GetProjectUseCase,
    private val getProjectFilesUseCase: GetProjectFilesUseCase,
    private val getJamProjectsUseCase: GetJamProjectsUseCase,
    private val createProjectUseCase: CreateProjectUseCase,
    private val submitProjectUseCase: SubmitProjectUseCase,
    private val returnDraftUseCase: ReturnDraftUseCase,
    private val disqualifyProjectUseCase: DisqualifyProjectUseCase,
    private val uploadProjectFileUseCase: UploadProjectFileUseCase,
    private val deleteProjectFileUseCase: DeleteProjectFileUseCase,
    private val deleteProjectUseCase: DeleteProjectUseCase,
    private val getJamStatisticsUseCase: GetJamStatisticsUseCase,
    private val updateRegistrationStatusUseCase: UpdateRegistrationStatusUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProgressState())
    val state: StateFlow<ProgressState> = _state.asStateFlow()

    fun load(jamId: String, isAdminOrOrganizer: Boolean) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            when (val teamsResult = getUserTeamsUseCase()){
                is ApiResult.Success -> {
                    _state.update { it.copy(userTeams = teamsResult.data) }
                }
                is ApiResult.Error -> {
                    _state.update { it.copy(errorMessage = teamsResult.message) }
                }
            }

            when (val registrationsResult = getJamRegistrationsUseCase(jamId)){
                is ApiResult.Success -> {
                    _state.update { it.copy(registrations = registrationsResult.data) }
                }
                is ApiResult.Error -> {
                    _state.update { it.copy(errorMessage = registrationsResult.message) }
                }
            }

            val approvedReg = _state.value.getUserRegistration()
            if (approvedReg != null) {
                when (val projectsResult = getTeamProjectsUseCase(approvedReg.teamId)){
                    is ApiResult.Success -> {
                        val project = projectsResult.data.firstOrNull { it.jamId == jamId }
                        if (project != null) {
                            loadProjectDetails(project.id)
                        }
                    }
                    is ApiResult.Error -> {
                        _state.update { it.copy(errorMessage = projectsResult.message) }
                    }
                }
            }

            if (isAdminOrOrganizer) {
                loadAllProjects(jamId)
                loadStatistics(jamId)
            }

            _state.update { it.copy(isLoading = false) }
        }
    }

    private suspend fun loadProjectDetails(projectId: String) {
        when (val result = getProjectUseCase(projectId)) {
            is ApiResult.Success -> {
                _state.update { it.copy(userProject = result.data) }
                loadProjectFiles(projectId)
            }
            is ApiResult.Error -> {
                _state.update { it.copy(errorMessage = result.message) }
            }
        }
    }

    private suspend fun loadProjectFiles(projectId: String) {
        when (val result = getProjectFilesUseCase(projectId)) {
            is ApiResult.Success -> {
                _state.update { it.copy(projectFiles = result.data) }
            }
            is ApiResult.Error -> {
                _state.update { it.copy(errorMessage = result.message) }
            }
        }
    }

    private suspend fun loadAllProjects(jamId: String) {
        val projects = ProjectStatus.entries.flatMap { status ->
            when (val result = getJamProjectsUseCase(jamId, status)) {
                is ApiResult.Success -> result.data
                else -> emptyList()
            }
        }.distinctBy { it.id }
        _state.update { it.copy(allProjects = projects) }
    }

    private suspend fun loadStatistics(jamId: String) {
        when (val result = getJamStatisticsUseCase(jamId)) {
            is ApiResult.Success -> _state.update { it.copy(statistics = result.data) }
            is ApiResult.Error -> {
                _state.update { it.copy(errorMessage = result.message) }
            }
        }
    }

    fun createProject(jamId: String, title: String, description: String?) {
        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            when (val result = createProjectUseCase(jamId, CreateProject(title, description))) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            userProject = result.data,
                            isActionLoading = false
                        )
                    }
                    loadProjectFiles(result.data.id)
                }
                is ApiResult.Error -> {
                    _state.update {
                        it.copy(
                            isActionLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun submitProject(projectId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            when (val result = submitProjectUseCase(projectId)) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            userProject = result.data,
                            isActionLoading = false
                        )
                    }
                }
                is ApiResult.Error -> {
                    _state.update {
                        it.copy(
                            isActionLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun cancelSubmission(projectId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            when (val result = returnDraftUseCase(projectId)) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            userProject = result.data,
                            isActionLoading = false
                        )
                    }
                }
                is ApiResult.Error -> {
                    _state.update {
                        it.copy(
                            isActionLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            when (val result = deleteProjectUseCase(projectId)) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            userProject = null,
                            projectFiles = emptyList(),
                            isActionLoading = false
                        )
                    }
                }
                is ApiResult.Error -> {
                    _state.update {
                        it.copy(
                            isActionLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun disqualifyProject(jamId: String, projectId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            when (val result = disqualifyProjectUseCase(projectId)) {
                is ApiResult.Success -> {
                    loadAllProjects(jamId)
                    _state.update { it.copy(isActionLoading = false) }
                }
                is ApiResult.Error -> {
                    _state.update {
                        it.copy(
                            isActionLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun disqualifyTeam(jamId: String, teamId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            when (val result = updateRegistrationStatusUseCase(
                jamId,
                teamId,
                UpdateRegistrationStatus(RegistrationStatus.DISQUALIFIED)
            )) {
                is ApiResult.Success -> {
                    _state.update { currentState ->
                        val updatedRegistrations = currentState.registrations.map {
                            if (it.teamId == teamId) it.copy(status = RegistrationStatus.DISQUALIFIED) else it
                        }
                        currentState.copy(
                            registrations = updatedRegistrations,
                            isActionLoading = false
                        )
                    }
                }
                is ApiResult.Error -> {
                    _state.update {
                        it.copy(
                            isActionLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun uploadFile(projectId: String, fileUpload: FileUpload) {
        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            val fileToUpload = ProjectFile(
                id = "",
                fileName = fileUpload.fileName,
                fileSize = fileUpload.bytes.size.toLong(),
                mimeType = fileUpload.mimeType,
                fileType = fileUpload.fileType,
                uploadedAt = "",
                uploadedByUserId = "",
                bytes = fileUpload.bytes
            )
            when (val result = uploadProjectFileUseCase(projectId, fileToUpload)) {
                is ApiResult.Success -> {
                    loadProjectFiles(projectId)
                    _state.update { it.copy(isActionLoading = false) }
                }
                is ApiResult.Error -> {
                    _state.update {
                        it.copy(
                            isActionLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun deleteFile(projectId: String, fileId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            when (val result = deleteProjectFileUseCase(projectId, fileId)) {
                is ApiResult.Success -> {
                    loadProjectFiles(projectId)
                    _state.update { it.copy(isActionLoading = false) }
                }
                is ApiResult.Error -> {
                    _state.update {
                        it.copy(
                            isActionLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
}
