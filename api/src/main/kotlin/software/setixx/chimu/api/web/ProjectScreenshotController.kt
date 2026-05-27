package software.setixx.chimu.api.web

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import software.setixx.chimu.api.domain.ProjectFileType
import software.setixx.chimu.api.dto.ProjectFileResponse
import software.setixx.chimu.api.repository.ProjectFileRepository
import software.setixx.chimu.api.repository.UserRepository
import software.setixx.chimu.api.security.CustomUserDetails
import software.setixx.chimu.api.service.ProjectFileService
import software.setixx.chimu.api.service.SeaweedFsService
import java.util.UUID

@RestController
@RequestMapping("/api/projects/{projectId}/screenshots")
@Tag(name = "Project Screenshots", description = "Screenshot upload and download for game jam projects")
class ProjectScreenshotController(
    private val projectFileService: ProjectFileService,
    private val seaweedFsService: SeaweedFsService,
    private val userRepository: UserRepository,
    private val projectFileRepository: ProjectFileRepository
) {

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(
        summary = "Upload project screenshot",
        description = "Uploads a JPEG/PNG/WebP image. Any team member, jam must be IN_PROGRESS. Limit: 10 files, max 15 MB each."
    )
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Screenshot uploaded"),
        ApiResponse(responseCode = "400", description = "Validation error"),
        ApiResponse(responseCode = "403", description = "Not a team member or jam not in progress"),
        ApiResponse(responseCode = "404", description = "Project not found")
    )
    fun uploadScreenshot(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Parameter(description = "Project public ID") @PathVariable projectId: String,
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<ProjectFileResponse> {
        val user = userRepository.findByPublicIdAndDeletedAtIsNull(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        val response = projectFileService.uploadScreenshot(
            projectId = projectId,
            userId    = user.id!!,
            file      = file
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    @Operation(
        summary = "List project screenshots",
        description = "Returns screenshot metadata. Access depends on jam status: IN_PROGRESS=team only; JUDGING=team+judges+organizer+ADMIN; COMPLETED=all authenticated."
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Screenshot list"),
        ApiResponse(responseCode = "403", description = "Access denied"),
        ApiResponse(responseCode = "404", description = "Project not found")
    )
    fun listScreenshots(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Parameter(description = "Project public ID") @PathVariable projectId: String
    ): ResponseEntity<List<ProjectFileResponse>> {
        val user = userRepository.findByPublicIdAndDeletedAtIsNull(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        return ResponseEntity.ok(
            projectFileService.listFilesByType(projectId, user.id!!, user.role, ProjectFileType.SCREENSHOT)
        )
    }

    @GetMapping("/{fileId}")
    @Operation(
        summary = "Stream project screenshot",
        description = """
            Streams the image from SeaweedFS filer to the client without buffering in RAM.
            resolveDownload() is called synchronously first to verify the file exists and
            check access rights — any error at that stage is returned as a normal JSON response.
            The actual byte-streaming happens asynchronously inside StreamingResponseBody.
            Response uses inline Content-Disposition so browsers render the image directly.
        """
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Image stream"),
        ApiResponse(responseCode = "403", description = "Access denied"),
        ApiResponse(responseCode = "404", description = "Project or screenshot not found")
    )
    fun downloadScreenshot(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Parameter(description = "Project public ID") @PathVariable projectId: String,
        @Parameter(description = "File record ID") @PathVariable fileId: String
    ): ResponseEntity<StreamingResponseBody> {
        val user = userRepository.findByPublicIdAndDeletedAtIsNull(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        val file = projectFileRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(fileId))
            ?: throw IllegalStateException("File not found")
        val meta = projectFileService.resolveDownload(projectId, file.id!!, user.id!!, user.role)

        val streamingBody = StreamingResponseBody { outputStream ->
            seaweedFsService.streamToOutput(meta.filePath, outputStream)
        }

        return ResponseEntity.ok()
            .contentType(
                runCatching { MediaType.parseMediaType(meta.mimeType) }
                    .getOrDefault(MediaType.IMAGE_JPEG)
            )
            .body(streamingBody)
    }

    @DeleteMapping("/{fileId}")
    @Operation(
        summary = "Delete project screenshot",
        description = "Soft-deletes DB record + hard-deletes in filer. Team leader only."
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Deleted"),
        ApiResponse(responseCode = "403", description = "Not the team leader"),
        ApiResponse(responseCode = "404", description = "Project or screenshot not found")
    )
    fun deleteScreenshot(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Parameter(description = "Project public ID") @PathVariable projectId: String,
        @Parameter(description = "File record ID") @PathVariable fileId: String
    ): ResponseEntity<Map<String, String>> {
        val user = userRepository.findByPublicIdAndDeletedAtIsNull(userDetails.publicId)
            ?: throw IllegalStateException("User not found")
        val file = projectFileRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(fileId))
            ?: throw IllegalStateException("File not found")

        projectFileService.deleteFile(projectId, file.id!!, user.id!!)
        return ResponseEntity.ok(mapOf("message" to "Screenshot deleted successfully"))
    }
}