package software.setixx.chimu.api.service

import org.slf4j.LoggerFactory
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import software.setixx.chimu.api.domain.GameJamStatus
import software.setixx.chimu.api.domain.ProjectFile
import software.setixx.chimu.api.domain.ProjectFileType
import software.setixx.chimu.api.domain.ProjectStatus
import software.setixx.chimu.api.domain.UserRole
import software.setixx.chimu.api.dto.ProjectFileDownloadMeta
import software.setixx.chimu.api.dto.ProjectFileResponse
import software.setixx.chimu.api.repository.GameJamRepository
import software.setixx.chimu.api.repository.JamJudgeRepository
import software.setixx.chimu.api.repository.ProjectFileRepository
import software.setixx.chimu.api.repository.ProjectRepository
import software.setixx.chimu.api.repository.TeamMemberRepository
import software.setixx.chimu.api.repository.UserRepository
import java.util.UUID

@Service
class ProjectFileService(
    private val projectRepository: ProjectRepository,
    private val projectFileRepository: ProjectFileRepository,
    private val gameJamRepository: GameJamRepository,
    private val teamMemberRepository: TeamMemberRepository,
    private val jamJudgeRepository: JamJudgeRepository,
    private val userRepository: UserRepository,
    private val seaweedFsService: SeaweedFsService
) {

    private val log = LoggerFactory.getLogger(ProjectFileService::class.java)

    companion object {
        private const val MAX_BUILD_FILES = 5
        private val ALLOWED_BUILD_EXTENSIONS = setOf("zip", "rar", "7z")
        private val ALLOWED_BUILD_MIME_TYPES = setOf(
            "application/zip",
            "application/x-zip-compressed",
            "application/x-zip",
            "application/x-rar-compressed",
            "application/vnd.rar",
            "application/x-7z-compressed",
            "application/octet-stream"
        )

        private const val MAX_VIDEO_FILES = 3
        private const val MAX_VIDEO_SIZE = 500L * 1024 * 1024
        private val ALLOWED_VIDEO_EXTENSIONS = setOf("mp4", "webm", "mov")
        private val ALLOWED_VIDEO_MIME_TYPES = setOf(
            "video/mp4",
            "video/webm",
            "video/quicktime"
        )

        // SCREENSHOT
        private const val MAX_SCREENSHOT_FILES = 10
        private const val MAX_SCREENSHOT_SIZE = 15L * 1024 * 1024
        private val ALLOWED_IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "webp")
        private val ALLOWED_IMAGE_MIME_TYPES = setOf(
            "image/jpeg",
            "image/png",
            "image/webp"
        )
    }

    @Transactional
    fun uploadFile(
        projectId: String,
        userId: Long,
        userRole: UserRole,
        file: MultipartFile
    ): ProjectFileResponse {
        val project = projectRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(projectId))
            ?: throw IllegalArgumentException("Project not found")

        if (project.team.leader.id != userId) {
            throw AccessDeniedException("Only the team leader can upload project files")
        }

        val jam = gameJamRepository.findById(project.gameJam.id!!).orElseThrow()
        if (jam.status != GameJamStatus.IN_PROGRESS) {
            throw IllegalStateException(
                "Files can only be uploaded while the jam is in progress (current status: ${jam.status})"
            )
        }

        if (project.status != ProjectStatus.DRAFT) {
            throw IllegalStateException("Files of the submitted project cannot be uploaded")
        }

        validateBuildFile(file)

        val existingCount = projectFileRepository.countByProjectIdAndFileTypeAndDeletedAtIsNull(
            project.id!!, ProjectFileType.BUILD
        )
        if (existingCount >= MAX_BUILD_FILES) {
            throw IllegalStateException(
                "Maximum number of build files ($MAX_BUILD_FILES) already reached for this project"
            )
        }

        val uploader = userRepository.findById(userId).orElseThrow()
        val sanitizedName = sanitizeFileName(file.originalFilename ?: "build")
        val filePath = "projects/$projectId/${UUID.randomUUID()}-$sanitizedName"

        try {
            seaweedFsService.upload(
                filePath    = filePath,
                inputStream = file.inputStream,
                fileSize    = file.size,
                fileName    = sanitizedName,
                mimeType    = file.contentType ?: "application/octet-stream"
            )
        } catch (ex: Exception) {
            log.error("SeaweedFS upload failed for project {}: {}", projectId, ex.message, ex)
            throw IllegalStateException("File storage unavailable, please try again later")
        }

        val projectFile = ProjectFile(
            project    = project,
            fileType   = ProjectFileType.BUILD,
            fileUrl    = filePath,
            fileName   = sanitizedName,
            fileSize   = file.size,
            mimeType   = file.contentType ?: "application/octet-stream",
            uploadedBy = uploader
        )

        return toResponse(projectFileRepository.save(projectFile))
    }


    @Transactional
    fun uploadVideo(
        projectId: String,
        userId: Long,
        file: MultipartFile
    ): ProjectFileResponse {
        val project = projectRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(projectId))
            ?: throw IllegalArgumentException("Project not found")

        if (!teamMemberRepository.existsByTeamIdAndUserIdAndDeletedAtIsNull(project.team.id!!, userId)) {
            throw AccessDeniedException("Only team members can upload project videos")
        }

        val jam = gameJamRepository.findById(project.gameJam.id!!).orElseThrow()
        if (jam.status != GameJamStatus.IN_PROGRESS) {
            throw IllegalStateException(
                "Files can only be uploaded while the jam is in progress (current status: ${jam.status})"
            )
        }

        if (project.status != ProjectStatus.DRAFT) {
            throw IllegalStateException("Files of the submitted project cannot be uploaded")
        }

        validateVideoFile(file)

        val existingCount = projectFileRepository.countByProjectIdAndFileTypeAndDeletedAtIsNull(
            project.id!!, ProjectFileType.VIDEO
        )
        if (existingCount >= MAX_VIDEO_FILES) {
            throw IllegalStateException(
                "Maximum number of video files ($MAX_VIDEO_FILES) already reached for this project"
            )
        }

        val uploader = userRepository.findById(userId).orElseThrow()
        val sanitizedName = sanitizeFileName(file.originalFilename ?: "video")
        val filePath = "projects/$projectId/videos/${UUID.randomUUID()}-$sanitizedName"

        try {
            seaweedFsService.upload(
                filePath    = filePath,
                inputStream = file.inputStream,
                fileSize    = file.size,
                fileName    = sanitizedName,
                mimeType    = file.contentType ?: "video/mp4"
            )
        } catch (ex: Exception) {
            log.error("SeaweedFS video upload failed for project {}: {}", projectId, ex.message, ex)
            throw IllegalStateException("File storage unavailable, please try again later")
        }

        val projectFile = ProjectFile(
            project    = project,
            fileType   = ProjectFileType.VIDEO,
            fileUrl    = filePath,
            fileName   = sanitizedName,
            fileSize   = file.size,
            mimeType   = file.contentType ?: "video/mp4",
            uploadedBy = uploader
        )

        return toResponse(projectFileRepository.save(projectFile))
    }


    @Transactional
    fun uploadScreenshot(
        projectId: String,
        userId: Long,
        file: MultipartFile
    ): ProjectFileResponse {
        val project = projectRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(projectId))
            ?: throw IllegalArgumentException("Project not found")

        if (!teamMemberRepository.existsByTeamIdAndUserIdAndDeletedAtIsNull(project.team.id!!, userId)) {
            throw AccessDeniedException("Only team members can upload project screenshots")
        }

        val jam = gameJamRepository.findById(project.gameJam.id!!).orElseThrow()
        if (jam.status != GameJamStatus.IN_PROGRESS) {
            throw IllegalStateException(
                "Files can only be uploaded while the jam is in progress (current status: ${jam.status})"
            )
        }

        if (project.status != ProjectStatus.DRAFT) {
            throw IllegalStateException("Files of the submitted project cannot be uploaded")
        }

        validateScreenshotFile(file)

        val existingCount = projectFileRepository.countByProjectIdAndFileTypeAndDeletedAtIsNull(
            project.id!!, ProjectFileType.SCREENSHOT
        )
        if (existingCount >= MAX_SCREENSHOT_FILES) {
            throw IllegalStateException(
                "Maximum number of screenshots ($MAX_SCREENSHOT_FILES) already reached for this project"
            )
        }

        val uploader = userRepository.findById(userId).orElseThrow()
        val sanitizedName = sanitizeFileName(file.originalFilename ?: "screenshot")
        val filePath = "projects/$projectId/screenshots/${UUID.randomUUID()}-$sanitizedName"

        try {
            seaweedFsService.upload(
                filePath    = filePath,
                inputStream = file.inputStream,
                fileSize    = file.size,
                fileName    = sanitizedName,
                mimeType    = file.contentType ?: "image/jpeg"
            )
        } catch (ex: Exception) {
            log.error("SeaweedFS screenshot upload failed for project {}: {}", projectId, ex.message, ex)
            throw IllegalStateException("File storage unavailable, please try again later")
        }

        val projectFile = ProjectFile(
            project    = project,
            fileType   = ProjectFileType.SCREENSHOT,
            fileUrl    = filePath,
            fileName   = sanitizedName,
            fileSize   = file.size,
            mimeType   = file.contentType ?: "image/jpeg",
            uploadedBy = uploader
        )

        return toResponse(projectFileRepository.save(projectFile))
    }


    @Transactional(readOnly = true)
    fun listFiles(
        projectId: String,
        userId: Long,
        userRole: UserRole
    ): List<ProjectFileResponse> {
        val project = projectRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(projectId))
            ?: throw IllegalArgumentException("Project not found")

        checkReadAccess(project.team.id!!, project.gameJam.id!!, project.gameJam.organizer.id!!, userId, userRole)

        return projectFileRepository
            .findAllByProjectIdAndDeletedAtIsNull(project.id!!)
            .map { toResponse(it) }
    }

    @Transactional(readOnly = true)
    fun listFilesByType(
        projectId: String,
        userId: Long,
        userRole: UserRole,
        fileType: ProjectFileType
    ): List<ProjectFileResponse> {
        val project = projectRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(projectId))
            ?: throw IllegalArgumentException("Project not found")

        checkReadAccess(project.team.id!!, project.gameJam.id!!, project.gameJam.organizer.id!!, userId, userRole)

        return projectFileRepository
            .findAllByProjectIdAndDeletedAtIsNull(project.id!!)
            .filter { it.fileType == fileType }
            .map { toResponse(it) }
    }

    @Transactional(readOnly = true)
    fun resolveDownload(
        projectId: String,
        fileId: Long,
        userId: Long,
        userRole: UserRole
    ): ProjectFileDownloadMeta {
        val project = projectRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(projectId))
            ?: throw IllegalArgumentException("Project not found")

        checkReadAccess(project.team.id!!, project.gameJam.id!!, project.gameJam.organizer.id!!, userId, userRole)

        val file = projectFileRepository.findByIdAndProjectIdAndDeletedAtIsNull(fileId, project.id!!)
            ?: throw IllegalArgumentException("File not found")

        seaweedFsService.resolve(file.fileUrl)

        return ProjectFileDownloadMeta(
            id       = file.id!!,
            filePath = file.fileUrl,
            fileName = file.fileName,
            fileSize = file.fileSize,
            mimeType = file.mimeType
        )
    }


    @Transactional
    fun deleteFile(
        projectId: String,
        fileId: Long,
        userId: Long
    ) {
        val project = projectRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(projectId))
            ?: throw IllegalArgumentException("Project not found")

        if (project.team.leader.id != userId) {
            throw AccessDeniedException("Only the team leader can delete project files")
        }

        if (project.status != ProjectStatus.DRAFT) {
            throw IllegalArgumentException("Files of the submitted project cannot be deleted")
        }

        val file = projectFileRepository.findByIdAndProjectIdAndDeletedAtIsNull(fileId, project.id!!)
            ?: throw IllegalArgumentException("File not found")

        projectFileRepository.softDeleteById(file.id!!)
        seaweedFsService.delete(file.fileUrl)
    }


    private fun checkReadAccess(
        teamId: Long,
        jamId: Long,
        jamOrganizerId: Long,
        userId: Long,
        userRole: UserRole
    ) {
        if (userRole == UserRole.ADMIN) return

        val jam = gameJamRepository.findById(jamId).orElseThrow()

        val allowed = when (jam.status) {
            GameJamStatus.IN_PROGRESS -> {
                teamMemberRepository.existsByTeamIdAndUserIdAndDeletedAtIsNull(teamId, userId) || jamOrganizerId == userId
            }
            GameJamStatus.JUDGING -> {
                teamMemberRepository.existsByTeamIdAndUserIdAndDeletedAtIsNull(teamId, userId)
                        || jamOrganizerId == userId
                        || jamJudgeRepository.existsByGameJamIdAndJudgeIdAndDeletedAtIsNull(jamId, userId)
            }
            GameJamStatus.COMPLETED -> true
            else -> false
        }

        if (!allowed) {
            throw AccessDeniedException("You do not have access to files for this project at this time")
        }
    }

    private fun validateBuildFile(file: MultipartFile) {
        if (file.isEmpty) throw IllegalArgumentException("Uploaded file is empty")

        val extension = file.originalFilename
            ?.substringAfterLast(".", "")
            ?.lowercase()
            .orEmpty()

        if (extension !in ALLOWED_BUILD_EXTENSIONS) {
            throw IllegalArgumentException(
                "Unsupported file type '.$extension'. Only zip, rar, and 7z archives are allowed"
            )
        }

        val mimeType = file.contentType?.lowercase().orEmpty()
        if (mimeType.isNotEmpty() && mimeType !in ALLOWED_BUILD_MIME_TYPES) {
            throw IllegalArgumentException("Unsupported MIME type '$mimeType' for a build archive")
        }

        if (file.size > 1_073_741_824L) {
            throw IllegalArgumentException("File size exceeds the 1 GB limit")
        }
    }

    private fun validateVideoFile(file: MultipartFile) {
        if (file.isEmpty) throw IllegalArgumentException("Uploaded file is empty")

        val extension = file.originalFilename
            ?.substringAfterLast(".", "")
            ?.lowercase()
            .orEmpty()

        if (extension !in ALLOWED_VIDEO_EXTENSIONS) {
            throw IllegalArgumentException(
                "Unsupported file type '.$extension'. Only mp4, webm, and mov videos are allowed"
            )
        }

        val mimeType = file.contentType?.lowercase().orEmpty()
        if (mimeType.isNotEmpty() && mimeType !in ALLOWED_VIDEO_MIME_TYPES) {
            throw IllegalArgumentException("Unsupported MIME type '$mimeType' for a video file")
        }

        if (file.size > MAX_VIDEO_SIZE) {
            throw IllegalArgumentException("File size exceeds the 500 MB limit")
        }
    }

    private fun validateScreenshotFile(file: MultipartFile) {
        if (file.isEmpty) throw IllegalArgumentException("Uploaded file is empty")

        val extension = file.originalFilename
            ?.substringAfterLast(".", "")
            ?.lowercase()
            .orEmpty()

        if (extension !in ALLOWED_IMAGE_EXTENSIONS) {
            throw IllegalArgumentException(
                "Unsupported file type '.$extension'. Only JPEG, PNG, and WebP images are allowed"
            )
        }

        val mimeType = file.contentType?.lowercase().orEmpty()
        if (mimeType.isNotEmpty() && mimeType !in ALLOWED_IMAGE_MIME_TYPES) {
            throw IllegalArgumentException("Unsupported MIME type '$mimeType' for a screenshot")
        }

        if (file.size > MAX_SCREENSHOT_SIZE) {
            throw IllegalArgumentException("File size exceeds the 15 MB limit")
        }
    }

    private fun sanitizeFileName(name: String): String =
        name.replace(Regex("[/\\\\:*?\"<>|\\s]"), "_").take(200)

    private fun toResponse(file: ProjectFile): ProjectFileResponse =
        ProjectFileResponse(
            id               = file.publicId.toString(),
            fileName         = file.fileName,
            fileSize         = file.fileSize,
            mimeType         = file.mimeType,
            fileType         = file.fileType,
            uploadedAt       = file.uploadedAt?.toString() ?: "",
            uploadedByUserId = file.uploadedBy.publicId.toString()
        )
}