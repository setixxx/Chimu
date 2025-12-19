package software.setixx.chimu.api.web

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import software.setixx.chimu.api.domain.GameJamStatus
import software.setixx.chimu.api.dto.ChangeGameJamStatusRequest
import software.setixx.chimu.api.dto.CreateGameJamRequest
import software.setixx.chimu.api.dto.GameJamDetailsResponse
import software.setixx.chimu.api.dto.GameJamResponse
import software.setixx.chimu.api.dto.UpdateGameJamRequest
import software.setixx.chimu.api.repository.UserRepository
import software.setixx.chimu.api.security.CustomUserDetails
import software.setixx.chimu.api.service.GameJamService

@RestController
@RequestMapping("/api/jams")
class GameJamController(
    private val gameJamService: GameJamService,
    private val userRepository: UserRepository
) {

    @PostMapping
    fun createGameJam(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Valid @RequestBody request: CreateGameJamRequest
    ): ResponseEntity<GameJamDetailsResponse> {
        val user = userRepository.findByPublicId(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        val jam = gameJamService.createGameJam(user.id!!, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(jam)
    }

    @GetMapping
    fun getAllGameJams(
        @RequestParam(required = false) status: GameJamStatus?
    ): ResponseEntity<List<GameJamResponse>> {
        val jams = gameJamService.getAllGameJams(status)
        return ResponseEntity.ok(jams)
    }

    @GetMapping("/{jamId}")
    fun getGameJamById(
        @PathVariable jamId: String
    ): ResponseEntity<GameJamDetailsResponse> {
        val jam = gameJamService.getGameJamById(jamId)
        return ResponseEntity.ok(jam)
    }

    @PatchMapping("/{jamId}")
    fun updateGameJam(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PathVariable jamId: String,
        @Valid @RequestBody request: UpdateGameJamRequest
    ): ResponseEntity<GameJamDetailsResponse> {
        val user = userRepository.findByPublicId(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        val jam = gameJamService.updateGameJam(jamId, user.id!!, request)
        return ResponseEntity.ok(jam)
    }

    @PostMapping("/{jamId}/status")
    fun changeGameJamStatus(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PathVariable jamId: String,
        @Valid @RequestBody request: ChangeGameJamStatusRequest
    ): ResponseEntity<GameJamDetailsResponse> {
        val user = userRepository.findByPublicId(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        val jam = gameJamService.changeGameJamStatus(jamId, user.id!!, request.status)
        return ResponseEntity.ok(jam)
    }

    @DeleteMapping("/{jamId}")
    fun deleteGameJam(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PathVariable jamId: String
    ): ResponseEntity<Map<String, String>> {
        val user = userRepository.findByPublicId(userDetails.publicId)
            ?: throw IllegalStateException("User not found")

        gameJamService.deleteGameJam(jamId, user.id!!)
        return ResponseEntity.ok(mapOf("message" to "Game jam deleted successfully"))
    }
}