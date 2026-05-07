package software.setixx.chimu.presentation.jam.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import software.setixx.chimu.api.domain.GameJamStatus
import software.setixx.chimu.api.domain.ProjectStatus
import software.setixx.chimu.api.domain.UserRole
import software.setixx.chimu.domain.model.*
import software.setixx.chimu.domain.usecase.*

class JamDetailsViewModel(
    private val getJamDetailsUseCase: GetJamDetailsUseCase,
    private val deleteJamUseCase: DeleteJamUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getJamRegistrationsUseCase: GetJamRegistrationsUseCase,
    private val registerTeamUseCase: RegisterTeamUseCase,
    private val withdrawTeamUseCase: WithdrawTeamUseCase,
    private val updateRegistrationStatusUseCase: UpdateRegistrationStatusUseCase,
    private val getUserTeamsUseCase: GetUserTeamsUseCase,
    private val getTeamDetailsUseCase: GetTeamDetailsUseCase,
    private val getJamJudgesUseCase: GetJamJudgesUseCase,
    private val assignJudgeUseCase: AssignJudgeUseCase,
    private val unassignJudgeUseCase: UnassignJudgeUseCase,
    private val getJamCriteriaUseCase: GetJamCriteriaUseCase,
    private val createJamCriteriaUseCase: CreateJamCriteriaUseCase,
    private val updateJamCriteriaUseCase: UpdateJamCriteriaUseCase,
    private val deleteJamCriteriaUseCase: DeleteJamCriteriaUseCase,
    private val getTeamProjectsUseCase: GetTeamProjectsUseCase,
    private val createProjectUseCase: CreateProjectUseCase,
    private val submitProjectUseCase: SubmitProjectUseCase,
    private val returnDraftUseCase: ReturnDraftUseCase,
    private val disqualifyProjectUseCase: DisqualifyProjectUseCase,
    private val getJamProjectsUseCase: GetJamProjectsUseCase,
    private val getProjectUseCase: GetProjectUseCase,
    private val uploadProjectFileUseCase: UploadProjectFileUseCase,
    private val getProjectFilesUseCase: GetProjectFilesUseCase,
    private val deleteProjectFileUseCase: DeleteProjectFileUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(JamDetailsState())
    val state: StateFlow<JamDetailsState> = _state.asStateFlow()

    fun loadJamDetails(jamId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            val userResult = getCurrentUserUseCase()
            if (userResult is ApiResult.Success) {
                val user = userResult.data
                _state.value = _state.value.copy(
                    userRole = user.role,
                    userId = user.id
                )
                
                if (user.role == UserRole.PARTICIPANT) {
                    loadUserTeams()
                }
            }

            loadRegistrations(jamId)
            loadJudges(jamId)
            loadCriteria(jamId)

            when (val result = getJamDetailsUseCase(jamId)) {
                is ApiResult.Success -> {
                    val jam = result.data
                    _state.value = _state.value.copy(
                        jamDetails = jam
                    )

                    if (jam.status == GameJamStatus.IN_PROGRESS.name) {
                        loadInProgressData(jamId)
                    }
                    _state.value = _state.value.copy(isLoading = false)
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(
                        errorMessage = result.message,
                        isLoading = false
                    )
                }
            }
        }
    }

    private suspend fun loadInProgressData(jamId: String) {
        val currentState = _state.value
        if (currentState.isParticipant) {
            val approvedReg = currentState.getUserRegistration()
            if (approvedReg != null) {
                when (val projectsResult = getTeamProjectsUseCase(approvedReg.teamId)) {
                    is ApiResult.Success -> {
                        val project = projectsResult.data.firstOrNull()
                        if (project != null) {
                            loadProjectDetails(project.id)
                        }
                    }
                    else -> {}
                }
            }
        }

        if (currentState.isAdminOrOrganizer) {
            loadAllProjects(jamId)
        }
    }

    private suspend fun loadProjectDetails(projectId: String) {
        when (val result = getProjectUseCase(projectId)) {
            is ApiResult.Success -> {
                _state.value = _state.value.copy(userProject = result.data)
                loadProjectFiles(projectId)
            }
            else -> {}
        }
    }

    private suspend fun loadProjectFiles(projectId: String) {
        when (val result = getProjectFilesUseCase(projectId)) {
            is ApiResult.Success -> {
                _state.value = _state.value.copy(projectFiles = result.data)
            }
            else -> {}
        }
    }

    fun uploadFile(projectId: String, fileUpload: FileUpload) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isActionLoading = true)
            // Mapping FileUpload to ProjectFile (using bytes)
            val fileToUpload = ProjectFile(
                id = "", // Backend will assign
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
                    _state.value = _state.value.copy(isActionLoading = false)
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(isActionLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun deleteFile(projectId: String, fileId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isActionLoading = true)
            when (val result = deleteProjectFileUseCase(projectId, fileId)) {
                is ApiResult.Success -> {
                    loadProjectFiles(projectId)
                    _state.value = _state.value.copy(isActionLoading = false)
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(isActionLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    private suspend fun loadAllProjects(jamId: String) {
        when (val result = getJamProjectsUseCase(jamId, ProjectStatus.SUBMITTED)) {
            is ApiResult.Success -> {
                _state.value = _state.value.copy(allProjects = result.data)
            }
            else -> {}
        }
    }

    private suspend fun loadUserTeams() {
        when (val result = getUserTeamsUseCase()) {
            is ApiResult.Success -> {
                _state.value = _state.value.copy(userTeams = result.data)
            }
            else -> {}
        }
    }

    fun createProject(jamId: String, title: String, description: String?) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isActionLoading = true)
            when (val result = createProjectUseCase(jamId, CreateProject(title, description))) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(userProject = result.data, isActionLoading = false)
                    loadProjectFiles(result.data.id)
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(isActionLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun submitProject(projectId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isActionLoading = true)
            when (val result = submitProjectUseCase(projectId)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(userProject = result.data, isActionLoading = false)
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(isActionLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun cancelSubmission(projectId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isActionLoading = true)
            when (val result = returnDraftUseCase(projectId)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(userProject = result.data, isActionLoading = false)
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(isActionLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun disqualifyProject(jamId: String, projectId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isActionLoading = true)
            when (val result = disqualifyProjectUseCase(projectId)) {
                is ApiResult.Success -> {
                    loadAllProjects(jamId)
                    _state.value = _state.value.copy(isActionLoading = false)
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(isActionLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    private suspend fun loadRegistrations(jamId: String) {
        when (val result = getJamRegistrationsUseCase(jamId)) {
            is ApiResult.Success -> {
                _state.value = _state.value.copy(registrations = result.data)
            }
            else -> {}
        }
    }

    private suspend fun loadJudges(jamId: String) {
        when (val result = getJamJudgesUseCase(jamId)) {
            is ApiResult.Success -> {
                _state.value = _state.value.copy(judges = result.data)
            }
            else -> {}
        }
    }

    private suspend fun loadCriteria(jamId: String) {
        when (val result = getJamCriteriaUseCase(jamId)) {
            is ApiResult.Success -> {
                _state.value = _state.value.copy(criteria = result.data)
            }
            else -> {}
        }
    }

    fun assignJudge(jamId: String, judgeUserId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isActionLoading = true)
            when (val result = assignJudgeUseCase(jamId, AssignJudge(judgeUserId))) {
                is ApiResult.Success -> {
                    loadJudges(jamId)
                    _state.value = _state.value.copy(isActionLoading = false)
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(isActionLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun unassignJudge(jamId: String, judgeUserId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isActionLoading = true)
            when (val result = unassignJudgeUseCase(jamId, judgeUserId)) {
                is ApiResult.Success -> {
                    loadJudges(jamId)
                    _state.value = _state.value.copy(isActionLoading = false)
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(isActionLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun createCriteria(jamId: String, data: CreateRatingCriteria) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isActionLoading = true)
            when (val result = createJamCriteriaUseCase(jamId, data)) {
                is ApiResult.Success -> {
                    loadCriteria(jamId)
                    _state.value = _state.value.copy(isActionLoading = false)
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(isActionLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun updateCriteria(jamId: String, criteriaId: Long, data: UpdateRatingCriteria) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isActionLoading = true)
            when (val result = updateJamCriteriaUseCase(jamId, criteriaId, data)) {
                is ApiResult.Success -> {
                    loadCriteria(jamId)
                    _state.value = _state.value.copy(isActionLoading = false)
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(isActionLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun deleteCriteria(jamId: String, criteriaId: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isActionLoading = true)
            when (val result = deleteJamCriteriaUseCase(jamId, criteriaId)) {
                is ApiResult.Success -> {
                    loadCriteria(jamId)
                    _state.value = _state.value.copy(isActionLoading = false)
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(isActionLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun registerTeam(jamId: String, teamId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isActionLoading = true)
            
            when (val teamDetailsResult = getTeamDetailsUseCase(teamId)) {
                is ApiResult.Success -> {
                    val membersWithoutSpec = teamDetailsResult.data.members.filter { it.specialization == null }
                    if (membersWithoutSpec.isNotEmpty()) {
                        val names = membersWithoutSpec.joinToString { it.nickname }
                        _state.value = _state.value.copy(
                            isActionLoading = false,
                            errorMessage = "У следующих участников не указана специализация: $names"
                        )
                        return@launch
                    }
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(isActionLoading = false, errorMessage = teamDetailsResult.message)
                    return@launch
                }
            }

            when (val result = registerTeamUseCase(jamId, RegisterTeam(teamId))) {
                is ApiResult.Success -> {
                    loadRegistrations(jamId)
                    _state.value = _state.value.copy(isActionLoading = false)
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(isActionLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun withdrawTeam(jamId: String, teamId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isActionLoading = true)
            when (val result = withdrawTeamUseCase(jamId, teamId)) {
                is ApiResult.Success -> {
                    loadRegistrations(jamId)
                    _state.value = _state.value.copy(isActionLoading = false)
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(isActionLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun updateRegistrationStatus(jamId: String, teamId: String, status: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isActionLoading = true)
            when (val result = updateRegistrationStatusUseCase(jamId, teamId, UpdateRegistrationStatus(status))) {
                is ApiResult.Success -> {
                    loadRegistrations(jamId)
                    _state.value = _state.value.copy(isActionLoading = false)
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(isActionLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun deleteJam(jamId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isDeleting = true)
            when (val result = deleteJamUseCase(jamId)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(isDeleting = false, isDeleted = true)
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(isDeleting = false, errorMessage = result.message)
                }
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}