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
        val jam = gameJamRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(jamId))
            ?: throw IllegalArgumentException("Game jam not found")

        val userTeams = teamRepository.findAllActiveByMemberId(userId)
        val registeredTeams = registrationRepository.findAllByGameJamIdAndDeletedAtIsNull(jam.id!!)
            .filter { it.status == RegistrationStatus.APPROVED }
            .map { it.team.id }

        val userApprovedTeam = userTeams.find {
            it.id in registeredTeams && it.leader.id == userId
        } ?: throw IllegalArgumentException("You must be a team leader of an approved team for this jam")

        if (jam.status != GameJamStatus.IN_PROGRESS) {
            throw IllegalStateException("Projects can only be uploaded at the in progress stage.")
        }

        val existingProject = projectRepository.findByTeamIdAndGameJamIdAndDeletedAtIsNull(userApprovedTeam.id!!, jam.id!!)
        if (existingProject != null) {
            throw IllegalArgumentException("Your team already has a project for this jam")
        }

        val project = Project(
            gameJam = jam,
            team = userApprovedTeam,
            title = request.title,
            description = request.description,
            gameUrl = request.gameUrl,
            status = ProjectStatus.DRAFT
        )

        val saved = projectRepository.save(project)
        return toDetailsResponse(saved, jam, userApprovedTeam, userId)
    }

    @Transactional(readOnly = true)
    fun getProjectById(projectId: String, userId: Long?): ProjectDetailsResponse {
        val project = projectRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(projectId))
            ?: throw IllegalArgumentException("Project not found")

        val jam = gameJamRepository.findById(project.gameJam.id!!).orElseThrow()
        val team = project.team.id?.let { teamRepository.findById(it).orElse(null) }

        return toDetailsResponse(project, jam, team, userId)
    }

    @Transactional(readOnly = true)
    fun getJamProjects(jamId: String, userId: Long?, status: ProjectStatus?): List<ProjectResponse> {
        val jam = gameJamRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(jamId))
            ?: throw IllegalArgumentException("Game jam not found")

        val user = userId?.let { userRepository.findById(it).orElse(null) }

        val projects = if (status != null) {
            projectRepository.findAllByGameJamIdAndStatusAndDeletedAtIsNull(jam.id!!, status)
        } else {
            projectRepository.findAllByGameJamIdAndDeletedAtIsNull(jam.id!!)
        }

        return projects
            .filter { canViewProject(it, jam, userId, user?.role) }
            .map { project ->
                val team = project.team.id?.let { teamRepository.findById(it).orElse(null) }
                toResponse(project, jam, team)
            }
    }

    @Transactional(readOnly = true)
    fun getTeamProjects(teamId: String): List<ProjectResponse> {
        val team = teamRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(teamId))
            ?: throw IllegalArgumentException("Team not found")

        val projects = projectRepository.findAllByTeamIdAndDeletedAtIsNull(team.id!!)

        return projects.map { project ->
            val jam = gameJamRepository.findById(project.gameJam.id!!).orElseThrow()
            toResponse(project, jam, team)
        }
    }

    @Transactional(readOnly = true)
    fun getMyProjects(userId: Long): List<ProjectResponse> {
        val projects = projectRepository.findAllByUserId(userId)

        return projects.map { project ->
            val jam = gameJamRepository.findById(project.gameJam.id!!).orElseThrow()
            val team = project.team.id?.let { teamRepository.findById(it).orElse(null) }
            toResponse(project, jam, team)
        }
    }

    @Transactional
    fun updateProject(projectId: String, userId: Long, request: UpdateProjectRequest): ProjectDetailsResponse {
        val project = projectRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(projectId))
            ?: throw IllegalArgumentException("Project not found")

        val team = project.team.id?.let { teamRepository.findById(it).orElse(null) }
            ?: throw IllegalArgumentException("Project has no associated team")

        if (team.leader.id != userId) {
            throw IllegalArgumentException("Only team leader can update the project")
        }

        validateApprovedRegistration(project, team)

        if (project.status != ProjectStatus.DRAFT) {
            throw IllegalArgumentException("Only draft projects can be edited")
        }

        request.title?.let { project.title = it }
        request.description?.let { project.description = it }
        request.gameUrl?.let { project.gameUrl = it }

        projectRepository.save(project)

        val jam = gameJamRepository.findById(project.gameJam.id!!).orElseThrow()
        return toDetailsResponse(project, jam, team, userId)
    }

    @Transactional
    fun submitProject(projectId: String, userId: Long): ProjectDetailsResponse {
        val project = projectRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(projectId))
            ?: throw IllegalArgumentException("Project not found")

        val jam = gameJamRepository.findById(project.gameJam.id!!).orElseThrow()
        val team = project.team.id?.let { teamRepository.findById(it).orElse(null) }
            ?: throw IllegalArgumentException("Project has no associated team")

        if (team.leader.id != userId) {
            throw IllegalArgumentException("Only team leader can submit the project")
        }

        validateApprovedRegistration(project, team)

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
        val project = projectRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(projectId))
            ?: throw IllegalArgumentException("Project not found")

        val jam = gameJamRepository.findById(project.gameJam.id!!).orElseThrow()
        val user = userRepository.findById(userId).orElseThrow()
        val team = project.team.id?.let { teamRepository.findById(it).orElse(null) }
        val teamLeader = teamRepository.findByLeaderIdAndDeletedAtIsNull(user.id!!)

        if (user.id != teamLeader?.id) {
            throw IllegalArgumentException("Only team leader can return project to draft")
        }

        if (team != null) {
            validateApprovedRegistration(project, team)
        }

        if (project.status != ProjectStatus.SUBMITTED) {
            throw IllegalArgumentException("Only submitted projects can be returned to draft")
        }

        if (jam.status != GameJamStatus.IN_PROGRESS) {
            throw IllegalArgumentException("Cannot return to draft after jam is completed")
        }

        project.status = ProjectStatus.DRAFT
        project.submittedAt = null
        projectRepository.save(project)

        return toDetailsResponse(project, jam, team, userId)
    }

    @Transactional
    fun disqualifyProject(projectId: String, userId: Long): ProjectDetailsResponse {
        val project = projectRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(projectId))
            ?: throw IllegalArgumentException("Project not found")

        val jam = gameJamRepository.findById(project.gameJam.id!!).orElseThrow()
        val user = userRepository.findById(userId).orElseThrow()

        if (jam.organizer.id != userId && user.role != UserRole.ADMIN) {
            throw IllegalArgumentException("Only organizer or admin can disqualify projects")
        }

        project.status = ProjectStatus.DISQUALIFIED
        projectRepository.save(project)

        val team = project.team.id?.let { teamRepository.findById(it).orElse(null) }
        return toDetailsResponse(project, jam, team, userId)
    }

    @Transactional
    fun deleteProject(projectId: String, userId: Long) {
        val project = projectRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(projectId))
            ?: throw IllegalArgumentException("Project not found")

        val team = project.team.id?.let { teamRepository.findById(it).orElse(null) }
            ?: throw IllegalArgumentException("Project has no associated team")

        if (team.leader.id != userId) {
            throw IllegalArgumentException("Only team leader can delete the project")
        }

        validateApprovedRegistration(project, team)

        if (project.status != ProjectStatus.DRAFT) {
            throw IllegalArgumentException("Only draft projects can be deleted")
        }

        if (project.gameJam.status == GameJamStatus.JUDGING) {
            throw IllegalStateException("Deleting a project is prohibited at the judging stage.")
        }

        projectRepository.softDeleteById(project.id!!)
    }

    private fun validateApprovedRegistration(project: Project, team: Team) {
        val registration = registrationRepository.findByGameJamIdAndTeamIdAndDeletedAtIsNull(
            project.gameJam.id!!,
            team.id!!
        ) ?: throw IllegalArgumentException("Team is not registered for this jam")

        if (registration.status != RegistrationStatus.APPROVED) {
            throw IllegalArgumentException("Team is no longer approved for this jam")
        }
    }

    private fun canViewProject(project: Project, jam: GameJam, userId: Long?, userRole: UserRole?): Boolean {
        return when (project.status) {
            ProjectStatus.DRAFT, ProjectStatus.SUBMITTED, ProjectStatus.UNDER_REVIEW -> {
                if (userId == null) return false
                val team = project.team.id?.let { teamRepository.findById(it).orElse(null) }
                team != null && (team.leader.id == userId || jam.organizer.id == userId || userRole == UserRole.ADMIN)
            }
            ProjectStatus.DISQUALIFIED -> jam.organizer.id == userId || userRole == UserRole.ADMIN
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
        val canEdit = userId != null && team?.leader?.id == userId && project.status == ProjectStatus.DRAFT
        val canSubmit = userId != null && team?.leader?.id == userId &&
                project.status == ProjectStatus.DRAFT &&
                jam.status == GameJamStatus.IN_PROGRESS
        val canDelete = userId != null && team?.leader?.id == userId && project.status == ProjectStatus.DRAFT

        return ProjectDetailsResponse(
            id = project.publicId.toString(),
            jamId = jam.publicId.toString(),
            jamName = jam.name,
            teamId = team?.publicId?.toString(),
            teamName = team?.name,
            title = project.title,
            description = project.description,
            gameUrl = project.gameUrl,
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
