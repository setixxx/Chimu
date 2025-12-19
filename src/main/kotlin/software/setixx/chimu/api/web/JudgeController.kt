package software.setixx.chimu.api.web

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import software.setixx.chimu.api.dto.AssignJudgeRequest
import software.setixx.chimu.api.dto.JudgeResponse
import software.setixx.chimu.api.repository.UserRepository
import software.setixx.chimu.api.security.CustomUserDetails
import software.setixx.chimu.api.service.JudgeManagementService

@RestController
@RequestMapping("/api/jams/{jamId}/judges")
class JudgeController(
    private val judgeManagementService: JudgeManagementService,
    private val userRepository: UserRepository
) {

    @PostMapping
    fun assignJudge(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PathVariable jamId: String,
        @Valid @RequestBody request: AssignJudgeRequest
    ): ResponseEntity<JudgeResponse> {
        val user = userRepository.findByPublicId(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        val judge = judgeManagementService.assignJudge(jamId, user.id!!, request.judgeUserId)
        return ResponseEntity.status(HttpStatus.CREATED).body(judge)
    }

    @GetMapping
    fun getJamJudges(
        @PathVariable jamId: String
    ): ResponseEntity<List<JudgeResponse>> {
        val judges = judgeManagementService.getJamJudges(jamId)
        return ResponseEntity.ok(judges)
    }

    @DeleteMapping("/{judgeUserId}")
    fun removeJudge(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PathVariable jamId: String,
        @PathVariable judgeUserId: String
    ): ResponseEntity<Map<String, String>> {
        val user = userRepository.findByPublicId(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        judgeManagementService.removeJudge(jamId, user.id!!, judgeUserId)
        return ResponseEntity.ok(mapOf("message" to "Judge removed successfully"))
    }
}