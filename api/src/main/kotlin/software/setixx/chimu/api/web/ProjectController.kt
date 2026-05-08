package software.setixx.chimu.api.web

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import software.setixx.chimu.api.domain.ProjectStatus
import software.setixx.chimu.api.dto.*
import software.setixx.chimu.api.repository.UserRepository
import software.setixx.chimu.api.security.CustomUserDetails
import software.setixx.chimu.api.service.ProjectService

@RestController
@RequestMapping("/api")
@Tag(name = "Projects", description = "Project management for game jams")
class ProjectController(
    private val projectService: ProjectService,
    private val userRepository: UserRepository
) {

    @PostMapping("/jams/{jamId}/projects")
    @Operation(summary = "Create project", description = "Creates a new project for a game jam. Only team leaders of approved teams can create projects.")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Project created successfully"),
        ApiResponse(responseCode = "400", description = "Invalid request or not authorized"),
        ApiResponse(responseCode = "403", description = "Not a team leader or team not approved")
    )
    fun createProject(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Parameter(description = "Game jam public ID")
        @PathVariable jamId: String,
        @Valid @RequestBody request: CreateProjectRequest
    ): ResponseEntity<ProjectDetailsResponse> {
        val user = userRepository.findByPublicIdAndDeletedAtIsNull(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        val project = projectService.createProject(jamId, user.id!!, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(project)
    }

    @GetMapping("/projects/{projectId}")
    @Operation(summary = "Get project", description = "Retrieves project details")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Project retrieved successfully"),
        ApiResponse(responseCode = "404", description = "Project not found")
    )
    fun getProject(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Parameter(description = "Project public ID")
        @PathVariable projectId: String
    ): ResponseEntity<ProjectDetailsResponse> {
        val user = userRepository.findByPublicIdAndDeletedAtIsNull(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        val project = projectService.getProjectById(projectId, user.id)
        return ResponseEntity.ok(project)
    }

    @GetMapping("/jams/{jamId}/projects")
    @Operation(summary = "Get jam projects", description = "Retrieves all projects for a game jam")
    @ApiResponse(responseCode = "200", description = "Projects retrieved successfully")
    fun getJamProjects(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Parameter(description = "Game jam public ID")
        @PathVariable jamId: String,
        @Parameter(description = "Filter by status")
        @RequestParam(required = false) status: ProjectStatus?
    ): ResponseEntity<List<ProjectResponse>> {
        val user = userRepository.findByPublicIdAndDeletedAtIsNull(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        val projects = projectService.getJamProjects(jamId, user.id!!, status)
        return ResponseEntity.ok(projects)
    }

    @GetMapping("/teams/{teamId}/projects")
    @Operation(summary = "Get team projects", description = "Retrieves all projects for a team")
    @ApiResponse(responseCode = "200", description = "Projects retrieved successfully")
    fun getTeamProjects(
        @Parameter(description = "Team public ID")
        @PathVariable teamId: String
    ): ResponseEntity<List<ProjectResponse>> {
        val projects = projectService.getTeamProjects(teamId)
        return ResponseEntity.ok(projects)
    }

    @GetMapping("/users/me/projects")
    @Operation(summary = "Get my projects", description = "Retrieves all projects for the current user")
    @ApiResponse(responseCode = "200", description = "Projects retrieved successfully")
    fun getMyProjects(
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<List<ProjectResponse>> {
        val user = userRepository.findByPublicIdAndDeletedAtIsNull(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        val projects = projectService.getMyProjects(user.id!!)
        return ResponseEntity.ok(projects)
    }

    @PatchMapping("/projects/{projectId}")
    @Operation(summary = "Update project", description = "Updates project details. Only draft projects can be edited.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Project updated successfully"),
        ApiResponse(responseCode = "400", description = "Invalid request or project not in draft"),
        ApiResponse(responseCode = "403", description = "Not authorized to update")
    )
    fun updateProject(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Parameter(description = "Project public ID")
        @PathVariable projectId: String,
        @Valid @RequestBody request: UpdateProjectRequest
    ): ResponseEntity<ProjectDetailsResponse> {
        val user = userRepository.findByPublicIdAndDeletedAtIsNull(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        val project = projectService.updateProject(projectId, user.id!!, request)
        return ResponseEntity.ok(project)
    }

    @PostMapping("/projects/{projectId}/submit")
    @Operation(summary = "Submit project", description = "Submits a project for judging. Only during IN_PROGRESS phase.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Project submitted successfully"),
        ApiResponse(responseCode = "400", description = "Cannot submit at this time"),
        ApiResponse(responseCode = "403", description = "Not authorized to submit")
    )
    fun submitProject(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Parameter(description = "Project public ID")
        @PathVariable projectId: String
    ): ResponseEntity<ProjectDetailsResponse> {
        val user = userRepository.findByPublicIdAndDeletedAtIsNull(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        val project = projectService.submitProject(projectId, user.id!!)
        return ResponseEntity.ok(project)
    }

    @PostMapping("/projects/{projectId}/return-draft")
    @Operation(summary = "Return to draft", description = "Returns a submitted project to draft status. Team leader only.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Project returned to draft"),
        ApiResponse(responseCode = "403", description = "Not authorized")
    )
    fun returnToDraft(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Parameter(description = "Project public ID")
        @PathVariable projectId: String
    ): ResponseEntity<ProjectDetailsResponse> {
        val user = userRepository.findByPublicIdAndDeletedAtIsNull(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        val project = projectService.returnToDraft(projectId, user.id!!)
        return ResponseEntity.ok(project)
    }

    @PostMapping("/projects/{projectId}/disqualify")
    @Operation(summary = "Disqualify project", description = "Disqualifies a project. Organizers/admins only.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Project disqualified"),
        ApiResponse(responseCode = "403", description = "Not authorized")
    )
    fun disqualifyProject(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Parameter(description = "Project public ID")
        @PathVariable projectId: String
    ): ResponseEntity<ProjectDetailsResponse> {
        val user = userRepository.findByPublicIdAndDeletedAtIsNull(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        val project = projectService.disqualifyProject(projectId, user.id!!)
        return ResponseEntity.ok(project)
    }

    @DeleteMapping("/projects/{projectId}")
    @Operation(summary = "Delete project", description = "Deletes a draft project. Team leaders only.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Project deleted successfully"),
        ApiResponse(responseCode = "400", description = "Only draft projects can be deleted"),
        ApiResponse(responseCode = "403", description = "Not authorized")
    )
    fun deleteProject(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Parameter(description = "Project public ID")
        @PathVariable projectId: String
    ): ResponseEntity<Map<String, String>> {
        val user = userRepository.findByPublicIdAndDeletedAtIsNull(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        projectService.deleteProject(projectId, user.id!!)
        return ResponseEntity.ok(mapOf("message" to "Project deleted successfully"))
    }
}