package software.setixx.chimu.api.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import software.setixx.chimu.api.domain.*
import software.setixx.chimu.api.dto.*
import software.setixx.chimu.api.repository.*
import java.time.Instant
import java.util.UUID

@Service
class ProjectService(
    private val projectRepository: ProjectRepository,
    private val gameJamRepository: GameJamRepository,
    private val teamRepository: TeamRepository,
    private val registrationRepository: JamTeamRegistrationRepository,
    private val userRepository: UserRepository
) {

    @Transactional
    fun createProject(jamId: String, userId: Long, request: CreateProjectRequest): ProjectDetailsResponse {
        val jam = gameJamRepository.findByPublicId(UUID.fromString(jamId))
            ?: throw IllegalArgumentException("Game jam not found")

        val userTeams = teamRepository.findAllByMemberId(userId)
        val registeredTeams = registrationRepository.findAllByJamId(jam.id!!)
            .filter { it.status == RegistrationStatus.APPROVED }
            .map { it.teamId }

        val userApprovedTeam = userTeams.find {
            it.id in registeredTeams && it.leaderId == userId
        } ?: throw IllegalArgumentException("You must be a team leader of an approved team for this jam")

        if (jam.status !in listOf(GameJamStatus.REGISTRATION_CLOSED, GameJamStatus.IN_PROGRESS)) {
            throw IllegalArgumentException("Projects can only be created during or after registration closed")
        }

        val existingProject = projectRepository.findByTeamIdAndJamId(userApprovedTeam.id!!, jam.id!!)
        if (existingProject != null) {
            throw IllegalArgumentException("Your team already has a project for this jam")
        }

        val project = Project(
            jamId = jam.id!!,
            teamId = userApprovedTeam.id,
            title = request.title,
            description = request.description,
            gameUrl = request.gameUrl,
            repositoryUrl = request.repositoryUrl,
            status = ProjectStatus.DRAFT
        )

        val saved = projectRepository.save(project)
        return toDetailsResponse(saved, jam, userApprovedTeam, userId)
    }

    @Transactional(readOnly = true)
    fun getProjectById(projectId: String, userId: Long?): ProjectDetailsResponse {
        val project = projectRepository.findByPublicId(UUID.fromString(projectId))
            ?: throw IllegalArgumentException("Project not found")

        val jam = gameJamRepository.findById(project.jamId).orElseThrow()
        val team = project.teamId?.let { teamRepository.findById(it).orElse(null) }

        return toDetailsResponse(project, jam, team, userId)
    }

    @Transactional(readOnly = true)
    fun getJamProjects(jamId: String, userId: Long?, status: ProjectStatus?): List<ProjectResponse> {
        val jam = gameJamRepository.findByPublicId(UUID.fromString(jamId))
            ?: throw IllegalArgumentException("Game jam not found")

        val user = userId?.let { userRepository.findById(it).orElse(null) }

        val projects = if (status != null) {
            projectRepository.findAllByJamIdAndStatus(jam.id!!, status)
        } else {
            projectRepository.findAllByJamId(jam.id!!)
        }

        return projects
            .filter { canViewProject(it, jam, userId, user?.role) }
            .map { project ->
                val team = project.teamId?.let { teamRepository.findById(it).orElse(null) }
                toResponse(project, jam, team)
            }
    }

    @Transactional(readOnly = true)
    fun getTeamProjects(teamId: String): List<ProjectResponse> {
        val team = teamRepository.findByPublicId(UUID.fromString(teamId))
            ?: throw IllegalArgumentException("Team not found")

        val projects = projectRepository.findAllByTeamId(team.id!!)

        return projects.map { project ->
            val jam = gameJamRepository.findById(project.jamId).orElseThrow()
            toResponse(project, jam, team)
        }
    }

    @Transactional(readOnly = true)
    fun getMyProjects(userId: Long): List<ProjectResponse> {
        val projects = projectRepository.findAllByUserId(userId)

        return projects.map { project ->
            val jam = gameJamRepository.findById(project.jamId).orElseThrow()
            val team = project.teamId?.let { teamRepository.findById(it).orElse(null) }
            toResponse(project, jam, team)
        }
    }

    @Transactional
    fun updateProject(projectId: String, userId: Long, request: UpdateProjectRequest): ProjectDetailsResponse {
        val project = projectRepository.findByPublicId(UUID.fromString(projectId))
            ?: throw IllegalArgumentException("Project not found")

        val team = project.teamId?.let { teamRepository.findById(it).orElse(null) }
            ?: throw IllegalArgumentException("Project has no associated team")

        if (team.leaderId != userId) {
            throw IllegalArgumentException("Only team leader can update the project")
        }

        if (project.status != ProjectStatus.DRAFT) {
            throw IllegalArgumentException("Only draft projects can be edited")
        }

        request.title?.let { project.title = it }
        request.description?.let { project.description = it }
        request.gameUrl?.let { project.gameUrl = it }
        request.repositoryUrl?.let { project.repositoryUrl = it }

        projectRepository.save(project)

        val jam = gameJamRepository.findById(project.jamId).orElseThrow()
        return toDetailsResponse(project, jam, team, userId)
    }

    @Transactional
    fun submitProject(projectId: String, userId: Long): ProjectDetailsResponse {
        val project = projectRepository.findByPublicId(UUID.fromString(projectId))
            ?: throw IllegalArgumentException("Project not found")

        val jam = gameJamRepository.findById(project.jamId).orElseThrow()
        val team = project.teamId?.let { teamRepository.findById(it).orElse(null) }
            ?: throw IllegalArgumentException("Project has no associated team")

        if (team.leaderId != userId) {
            throw IllegalArgumentException("Only team leader can submit the project")
        }

        if (project.status != ProjectStatus.DRAFT) {
            throw IllegalArgumentException("Project is already submitted")
        }

        if (jam.status != GameJamStatus.IN_PROGRESS) {
            throw IllegalArgumentException("Projects can only be submitted during the jam period")
        }

        project.status = ProjectStatus.SUBMITTED
        project.submittedAt = Instant.now()
        projectRepository.save(project)

        return toDetailsResponse(project, jam, team, userId)
    }

    @Transactional
    fun returnToDraft(projectId: String, userId: Long): ProjectDetailsResponse {
        val project = projectRepository.findByPublicId(UUID.fromString(projectId))
            ?: throw IllegalArgumentException("Project not found")

        val jam = gameJamRepository.findById(project.jamId).orElseThrow()
        val user = userRepository.findById(userId).orElseThrow()

        if (jam.organizerId != userId && user.role != UserRole.ADMIN) {
            throw IllegalArgumentException("Only organizer or admin can return project to draft")
        }

        if (project.status != ProjectStatus.SUBMITTED) {
            throw IllegalArgumentException("Only submitted projects can be returned to draft")
        }

        if (jam.status !in listOf(GameJamStatus.IN_PROGRESS, GameJamStatus.JUDGING)) {
            throw IllegalArgumentException("Cannot return to draft after jam is completed")
        }

        project.status = ProjectStatus.DRAFT
        project.submittedAt = null
        projectRepository.save(project)

        val team = project.teamId?.let { teamRepository.findById(it).orElse(null) }
        return toDetailsResponse(project, jam, team, userId)
    }

    @Transactional
    fun publishProject(projectId: String, userId: Long): ProjectDetailsResponse {
        val project = projectRepository.findByPublicId(UUID.fromString(projectId))
            ?: throw IllegalArgumentException("Project not found")

        val jam = gameJamRepository.findById(project.jamId).orElseThrow()
        val user = userRepository.findById(userId).orElseThrow()

        if (jam.organizerId != userId && user.role != UserRole.ADMIN) {
            throw IllegalArgumentException("Only organizer or admin can publish projects")
        }

        if (project.status != ProjectStatus.SUBMITTED) {
            throw IllegalArgumentException("Only submitted projects can be published")
        }

        if (jam.status != GameJamStatus.JUDGING) {
            throw IllegalArgumentException("Projects can only be published during judging phase")
        }

        project.status = ProjectStatus.PUBLISHED
        projectRepository.save(project)

        val team = project.teamId?.let { teamRepository.findById(it).orElse(null) }
        return toDetailsResponse(project, jam, team, userId)
    }

    @Transactional
    fun disqualifyProject(projectId: String, userId: Long): ProjectDetailsResponse {
        val project = projectRepository.findByPublicId(UUID.fromString(projectId))
            ?: throw IllegalArgumentException("Project not found")

        val jam = gameJamRepository.findById(project.jamId).orElseThrow()
        val user = userRepository.findById(userId).orElseThrow()

        if (jam.organizerId != userId && user.role != UserRole.ADMIN) {
            throw IllegalArgumentException("Only organizer or admin can disqualify projects")
        }

        project.status = ProjectStatus.DISQUALIFIED
        projectRepository.save(project)

        val team = project.teamId?.let { teamRepository.findById(it).orElse(null) }
        return toDetailsResponse(project, jam, team, userId)
    }

    @Transactional
    fun deleteProject(projectId: String, userId: Long) {
        val project = projectRepository.findByPublicId(UUID.fromString(projectId))
            ?: throw IllegalArgumentException("Project not found")

        val team = project.teamId?.let { teamRepository.findById(it).orElse(null) }
            ?: throw IllegalArgumentException("Project has no associated team")

        if (team.leaderId != userId) {
            throw IllegalArgumentException("Only team leader can delete the project")
        }

        if (project.status != ProjectStatus.DRAFT) {
            throw IllegalArgumentException("Only draft projects can be deleted")
        }

        projectRepository.delete(project)
    }

    private fun canViewProject(project: Project, jam: GameJam, userId: Long?, userRole: UserRole?): Boolean {
        return when (project.status) {
            ProjectStatus.DRAFT -> {
                if (userId == null) return false
                val team = project.teamId?.let { teamRepository.findById(it).orElse(null) }
                team != null && (team.leaderId == userId || jam.organizerId == userId || userRole == UserRole.ADMIN)
            }
            ProjectStatus.SUBMITTED -> {
                if (userId == null) return false
                val team = project.teamId?.let { teamRepository.findById(it).orElse(null) }
                team != null && (team.leaderId == userId || jam.organizerId == userId || userRole == UserRole.ADMIN)
            }
            ProjectStatus.PUBLISHED -> true
            ProjectStatus.DISQUALIFIED -> jam.organizerId == userId || userRole == UserRole.ADMIN
            ProjectStatus.UNDER_REVIEW -> {
                if (userId == null) return false
                val team = project.teamId?.let { teamRepository.findById(it).orElse(null) }
                team != null && (team.leaderId == userId || jam.organizerId == userId || userRole == UserRole.ADMIN)
            }
        }
    }

    private fun toResponse(project: Project, jam: GameJam, team: Team?): ProjectResponse {
        return ProjectResponse(
            id = project.publicId.toString(),
            jamId = jam.publicId.toString(),
            jamName = jam.name,
            teamId = team?.publicId?.toString(),
            teamName = team?.name,
            title = project.title,
            description = project.description,
            gameUrl = project.gameUrl,
            repositoryUrl = project.repositoryUrl,
            status = project.status,
            submittedAt = project.submittedAt?.toString(),
            createdAt = project.createdAt.toString(),
            updatedAt = project.updatedAt.toString()
        )
    }

    private fun toDetailsResponse(
        project: Project,
        jam: GameJam,
        team: Team?,
        userId: Long?
    ): ProjectDetailsResponse {
        val canEdit = userId != null && team?.leaderId == userId && project.status == ProjectStatus.DRAFT
        val canSubmit = userId != null && team?.leaderId == userId &&
                project.status == ProjectStatus.DRAFT &&
                jam.status == GameJamStatus.IN_PROGRESS
        val canDelete = userId != null && team?.leaderId == userId && project.status == ProjectStatus.DRAFT

        return ProjectDetailsResponse(
            id = project.publicId.toString(),
            jamId = jam.publicId.toString(),
            jamName = jam.name,
            teamId = team?.publicId?.toString(),
            teamName = team?.name,
            title = project.title,
            description = project.description,
            gameUrl = project.gameUrl,
            repositoryUrl = project.repositoryUrl,
            status = project.status,
            submittedAt = project.submittedAt?.toString(),
            createdAt = project.createdAt.toString(),
            updatedAt = project.updatedAt.toString(),
            canEdit = canEdit,
            canSubmit = canSubmit,
            canDelete = canDelete
        )
    }
}