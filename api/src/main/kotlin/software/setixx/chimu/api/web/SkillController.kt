package software.setixx.chimu.api.web

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import software.setixx.chimu.api.dto.SkillResponse
import software.setixx.chimu.api.repository.SkillRepository
import kotlin.collections.map

@RestController
@RequestMapping("/api/skills")
@Tag(name = "Skills", description = "Skill management")
class SkillController(
    private val skillRepository: SkillRepository
) {
    @GetMapping
    @Operation(summary = "Get all skills", description = "Retrieves a list of all available skills")
    @ApiResponse(responseCode = "200", description = "Skills retrieved successfully")
    fun getAllSkills(): ResponseEntity<List<SkillResponse>> {
        val skills = skillRepository.findAll().map {
            SkillResponse(id = it.publicId.toString(), name = it.name)
        }
        return ResponseEntity.ok(skills)
    }
}