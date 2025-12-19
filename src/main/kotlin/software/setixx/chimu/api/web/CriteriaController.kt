package software.setixx.chimu.api.web

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import software.setixx.chimu.api.dto.CreateCriteriaRequest
import software.setixx.chimu.api.dto.CriteriaResponse
import software.setixx.chimu.api.dto.UpdateCriteriaRequest
import software.setixx.chimu.api.repository.UserRepository
import software.setixx.chimu.api.security.CustomUserDetails
import software.setixx.chimu.api.service.CriteriaService

@RestController
@RequestMapping("/api/jams/{jamId}/criteria")
class CriteriaController(
    private val criteriaService: CriteriaService,
    private val userRepository: UserRepository
) {

    @PostMapping
    fun createCriteria(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PathVariable jamId: String,
        @Valid @RequestBody request: CreateCriteriaRequest
    ): ResponseEntity<CriteriaResponse> {
        val user = userRepository.findByPublicId(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        val criteria = criteriaService.createCriteria(jamId, user.id!!, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(criteria)
    }

    @GetMapping
    fun getJamCriteria(
        @PathVariable jamId: String
    ): ResponseEntity<List<CriteriaResponse>> {
        val criteria = criteriaService.getJamCriteria(jamId)
        return ResponseEntity.ok(criteria)
    }

    @PatchMapping("/{criteriaId}")
    fun updateCriteria(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PathVariable jamId: String,
        @PathVariable criteriaId: Long,
        @Valid @RequestBody request: UpdateCriteriaRequest
    ): ResponseEntity<CriteriaResponse> {
        val user = userRepository.findByPublicId(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        val criteria = criteriaService.updateCriteria(jamId, criteriaId, user.id!!, request)
        return ResponseEntity.ok(criteria)
    }

    @DeleteMapping("/{criteriaId}")
    fun deleteCriteria(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PathVariable jamId: String,
        @PathVariable criteriaId: Long
    ): ResponseEntity<Map<String, String>> {
        val user = userRepository.findByPublicId(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        criteriaService.deleteCriteria(jamId, criteriaId, user.id!!)
        return ResponseEntity.ok(mapOf("message" to "Criteria deleted successfully"))
    }
}