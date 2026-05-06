package software.setixx.chimu.api.service

import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import software.setixx.chimu.api.domain.UserRole
import software.setixx.chimu.api.repository.GameJamRepository
import software.setixx.chimu.api.repository.UserRepository
import java.util.UUID

@Service
class JamBannerService(
    private val gameJamRepository: GameJamRepository,
    private val userRepository: UserRepository,
    private val seaweedFsService: SeaweedFsService
) {

    companion object {
        private const val MAX_SIZE = 15L * 1024 * 1024
        private val ALLOWED_MIME_TYPES = setOf("image/jpeg", "image/png", "image/webp")
        private val EXTENSION_MAP = mapOf(
            "image/jpeg" to "jpg",
            "image/png"  to "png",
            "image/webp" to "webp"
        )
    }

    @Transactional
    fun uploadBanner(jamId: String, userId: Long, file: MultipartFile) {
        val jam = gameJamRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(jamId))
            ?: throw IllegalArgumentException("Game jam not found")

        val user = userRepository.findById(userId).orElseThrow()
        if (jam.organizer.id != userId && user.role != UserRole.ADMIN) {
            throw AccessDeniedException("Only the organizer or admin can upload a banner")
        }

        validateFile(file)

        val mimeType = file.contentType!!.lowercase()
        val ext      = EXTENSION_MAP[mimeType] ?: "jpg"
        val filePath = "jams/${jam.publicId}/banner.$ext"

        jam.bannerUrl?.let { seaweedFsService.delete(it) }

        seaweedFsService.upload(
            filePath    = filePath,
            inputStream = file.inputStream,
            fileSize    = file.size,
            fileName    = "banner.$ext",
            mimeType    = mimeType
        )

        jam.bannerUrl = filePath
        gameJamRepository.save(jam)
    }

    @Transactional
    fun deleteBanner(jamId: String, userId: Long) {
        val jam = gameJamRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(jamId))
            ?: throw IllegalArgumentException("Game jam not found")

        val user = userRepository.findById(userId).orElseThrow()
        if (jam.organizer.id != userId && user.role != UserRole.ADMIN) {
            throw AccessDeniedException("Only the organizer or admin can delete a banner")
        }

        val path = jam.bannerUrl ?: throw IllegalArgumentException("This jam has no banner")

        seaweedFsService.delete(path)
        jam.bannerUrl = null
        gameJamRepository.save(jam)
    }

    fun resolveBannerPath(jamId: String): Pair<String, String> {
        val jam = gameJamRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(jamId))
            ?: throw IllegalArgumentException("Game jam not found")

        val path = jam.bannerUrl ?: throw IllegalArgumentException("This jam has no banner")

        val mimeType = when (path.substringAfterLast(".", "")) {
            "png"  -> "image/png"
            "webp" -> "image/webp"
            else   -> "image/jpeg"
        }

        return Pair(path, mimeType)
    }

    private fun validateFile(file: MultipartFile) {
        if (file.isEmpty) throw IllegalArgumentException("File is empty")

        if (file.size > MAX_SIZE) {
            throw IllegalArgumentException("File size exceeds the 15 MB limit")
        }

        val mimeType = file.contentType?.lowercase().orEmpty()
        if (mimeType !in ALLOWED_MIME_TYPES) {
            throw IllegalArgumentException("Unsupported file type. Only JPEG, PNG and WebP are allowed")
        }
    }
}