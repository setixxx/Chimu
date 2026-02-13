package software.setixx.chimu.api.web

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import software.setixx.chimu.api.dto.SkillResponse
import software.setixx.chimu.api.repository.SkillRepository
import kotlin.collections.map

@RestController
@RequestMapping("/api/skills")
class SkillController(
    private val skillRepository: SkillRepository
) {
    @GetMapping
    fun getAllSkills(): ResponseEntity<List<SkillResponse>> {
        val skills = skillRepository.findAll().map {
            SkillResponse(id = it.id!!, name = it.name)
        }
        return ResponseEntity.ok(skills)
    }
}