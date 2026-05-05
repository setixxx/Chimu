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
import software.setixx.chimu.api.dto.ProjectFileResponse
import software.setixx.chimu.api.repository.ProjectFileRepository
import software.setixx.chimu.api.repository.UserRepository
import software.setixx.chimu.api.security.CustomUserDetails
import software.setixx.chimu.api.service.ProjectFileService
import software.setixx.chimu.api.service.SeaweedFsService
import java.util.UUID

@RestController
@RequestMapping("/api/projects/{projectId}/files")
@Tag(name = "Project Files", description = "Build file upload and download for game jam projects")
class ProjectFileController(
    private val projectFileService: ProjectFileService,
    private val seaweedFsService: SeaweedFsService,
    private val userRepository: UserRepository,
    private val projectFileRepository: ProjectFileRepository
) {

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(
        summary = "Upload build file",
        description = "Uploads a zip/rar/7z archive. Team leader only, jam must be IN_PROGRESS. Limit: 5 files, max 1 GB each."
    )
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "File uploaded"),
        ApiResponse(responseCode = "400", description = "Validation error"),
        ApiResponse(responseCode = "403", description = "Not a team leader or jam not in progress"),
        ApiResponse(responseCode = "404", description = "Project not found")
    )
    fun uploadFile(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Parameter(description = "Project public ID") @PathVariable projectId: String,
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<ProjectFileResponse> {
        val user = userRepository.findByPublicIdAndDeletedAtIsNull(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        val response = projectFileService.uploadFile(
            projectId = projectId,
            userId    = user.id!!,
            userRole  = user.role,
            file      = file
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    @Operation(
        summary = "List project files",
        description = "Returns file metadata. Access depends on jam status: IN_PROGRESS=team only; JUDGING=team+judges+organizer+ADMIN; COMPLETED=all authenticated."
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "File list"),
        ApiResponse(responseCode = "403", description = "Access denied"),
        ApiResponse(responseCode = "404", description = "Project not found")
    )
    fun listFiles(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Parameter(description = "Project public ID") @PathVariable projectId: String
    ): ResponseEntity<List<ProjectFileResponse>> {
        val user = userRepository.findByPublicIdAndDeletedAtIsNull(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        return ResponseEntity.ok(
            projectFileService.listFiles(projectId, user.id!!, user.role)
        )
    }

    @GetMapping("/{fileId}")
    @Operation(
        summary = "Download build file",
        description = """
            Streams the file from SeaweedFS filer to the client without buffering in RAM.
            resolveDownload() is called synchronously first to verify the file exists and
            check access rights — any error at that stage is returned as a normal JSON response.
            The actual byte-streaming happens asynchronously inside StreamingResponseBody.
        """
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "File stream"),
        ApiResponse(responseCode = "403", description = "Access denied"),
        ApiResponse(responseCode = "404", description = "Project or file not found")
    )
    fun downloadFile(
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
        summary = "Delete build file",
        description = "Soft-deletes DB record + hard-deletes in filer. Team leader only."
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Deleted"),
        ApiResponse(responseCode = "403", description = "Not the team leader"),
        ApiResponse(responseCode = "404", description = "Project or file not found")
    )
    fun deleteFile(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Parameter(description = "Project public ID") @PathVariable projectId: String,
        @Parameter(description = "File record ID") @PathVariable fileId: String
    ): ResponseEntity<Map<String, String>> {
        val user = userRepository.findByPublicIdAndDeletedAtIsNull(userDetails.publicId)
            ?: throw IllegalStateException("User not found")
        val file = projectFileRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(fileId))
            ?: throw IllegalStateException("File not found")

        projectFileService.deleteFile(projectId, file.id!!, user.id!!)
        return ResponseEntity.ok(mapOf("message" to "File deleted successfully"))
    }
}