package software.setixx.chimu.api.web

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import software.setixx.chimu.api.repository.UserRepository
import software.setixx.chimu.api.security.CustomUserDetails
import software.setixx.chimu.api.service.JamBannerService
import software.setixx.chimu.api.service.SeaweedFsService

@RestController
@RequestMapping("/api/jams/{jamId}/banner")
@Tag(name = "Game Jams", description = "Game jam banner management")
class BannerController(
    private val jamBannerService: JamBannerService,
    private val seaweedFsService: SeaweedFsService,
    private val userRepository: UserRepository
) {

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(summary = "Upload jam banner", description = "Uploads or replaces the banner. JPEG, PNG, WebP; max 15 MB.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Banner uploaded successfully"),
        ApiResponse(responseCode = "400", description = "Validation error"),
        ApiResponse(responseCode = "403", description = "Not authorized")
    )
    fun uploadBanner(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Parameter(description = "Game jam public ID") @PathVariable jamId: String,
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<Map<String, String>> {
        val user = userRepository.findByPublicIdAndDeletedAtIsNull(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        jamBannerService.uploadBanner(jamId, user.id!!, file)
        return ResponseEntity.ok(mapOf("message" to "Banner uploaded successfully"))
    }

    @GetMapping
    @Operation(summary = "Get jam banner", description = "Streams the banner image. Publicly accessible.")
    @ApiResponse(responseCode = "200", description = "Image stream")
    fun getBanner(
        @Parameter(description = "Game jam public ID") @PathVariable jamId: String
    ): ResponseEntity<StreamingResponseBody> {
        val (filePath, mimeType) = jamBannerService.resolveBannerPath(jamId)

        val body = StreamingResponseBody { out ->
            seaweedFsService.streamToOutput(filePath, out)
        }

        return ResponseEntity.ok()
            .contentType(
                runCatching { MediaType.parseMediaType(mimeType) }
                    .getOrDefault(MediaType.APPLICATION_OCTET_STREAM)
            )
            .body(body)
    }

    @DeleteMapping
    @Operation(summary = "Delete jam banner")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Banner deleted successfully"),
        ApiResponse(responseCode = "403", description = "Not authorized")
    )
    fun deleteBanner(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Parameter(description = "Game jam public ID") @PathVariable jamId: String
    ): ResponseEntity<Map<String, String>> {
        val user = userRepository.findByPublicIdAndDeletedAtIsNull(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        jamBannerService.deleteBanner(jamId, user.id!!)
        return ResponseEntity.ok(mapOf("message" to "Banner deleted successfully"))
    }
}