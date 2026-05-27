package software.setixx.chimu.api.web

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpHeaders
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
@RequestMapping("/api/projects/{projectId}/videos")
@Tag(name = "Project Videos", description = "Video upload and download for game jam projects")
class ProjectVideoController(
    private val projectFileService: ProjectFileService,
    private val seaweedFsService: SeaweedFsService,
    private val userRepository: UserRepository,
    private val projectFileRepository: ProjectFileRepository
) {

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(
        summary = "Upload project video",
        description = "Uploads an mp4/webm/mov video. Any team member, jam must be IN_PROGRESS. Limit: 3 files, max 500 MB each."
    )
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Video uploaded"),
        ApiResponse(responseCode = "400", description = "Validation error"),
        ApiResponse(responseCode = "403", description = "Not a team member or jam not in progress"),
        ApiResponse(responseCode = "404", description = "Project not found")
    )
    fun uploadVideo(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Parameter(description = "Project public ID") @PathVariable projectId: String,
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<ProjectFileResponse> {
        val user = userRepository.findByPublicIdAndDeletedAtIsNull(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        val response = projectFileService.uploadVideo(
            projectId = projectId,
            userId    = user.id!!,
            file      = file
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    @Operation(
        summary = "List project videos",
        description = "Returns video metadata. Access depends on jam status: IN_PROGRESS=team only; JUDGING=team+judges+organizer+ADMIN; COMPLETED=all authenticated."
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Video list"),
        ApiResponse(responseCode = "403", description = "Access denied"),
        ApiResponse(responseCode = "404", description = "Project not found")
    )
    fun listVideos(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Parameter(description = "Project public ID") @PathVariable projectId: String
    ): ResponseEntity<List<ProjectFileResponse>> {
        val user = userRepository.findByPublicIdAndDeletedAtIsNull(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        return ResponseEntity.ok(
            projectFileService.listFilesByType(projectId, user.id!!, user.role, ProjectFileType.VIDEO)
        )
    }

    @GetMapping("/{fileId}")
    @Operation(
        summary = "Download project video",
        description = """
            Streams the video from SeaweedFS filer to the client without buffering in RAM.
            resolveDownload() is called synchronously first to verify the file exists and
            check access rights — any error at that stage is returned as a normal JSON response.
            The actual byte-streaming happens asynchronously inside StreamingResponseBody.
        """
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Video stream"),
        ApiResponse(responseCode = "403", description = "Access denied"),
        ApiResponse(responseCode = "404", description = "Project or video not found")
    )
    fun downloadVideo(
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
                    .getOrDefault(MediaType.APPLICATION_OCTET_STREAM)
            )
            .header(HttpHeaders.CONTENT_DISPOSITION, """attachment; filename="${meta.fileName}"""")
            .header(HttpHeaders.CONTENT_LENGTH, meta.fileSize.toString())
            .body(streamingBody)
    }

    @DeleteMapping("/{fileId}")
    @Operation(
        summary = "Delete project video",
        description = "Soft-deletes DB record + hard-deletes in filer. Team leader only."
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Deleted"),
        ApiResponse(responseCode = "403", description = "Not the team leader"),
        ApiResponse(responseCode = "404", description = "Project or video not found")
    )
    fun deleteVideo(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Parameter(description = "Project public ID") @PathVariable projectId: String,
        @Parameter(description = "File record ID") @PathVariable fileId: String
    ): ResponseEntity<Map<String, String>> {
        val user = userRepository.findByPublicIdAndDeletedAtIsNull(userDetails.publicId)
            ?: throw IllegalStateException("User not found")
        val file = projectFileRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(fileId))
            ?: throw IllegalStateException("File not found")

        projectFileService.deleteFile(projectId, file.id!!, user.id!!)
        return ResponseEntity.ok(mapOf("message" to "Video deleted successfully"))
    }
}