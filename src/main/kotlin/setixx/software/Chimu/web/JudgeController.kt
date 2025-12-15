package setixx.software.Chimu.web

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import setixx.software.Chimu.dto.AssignJudgeRequest
import setixx.software.Chimu.dto.JudgeResponse
import setixx.software.Chimu.repository.UserRepository
import setixx.software.Chimu.security.CustomUserDetails
import setixx.software.Chimu.service.JudgeManagementService

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