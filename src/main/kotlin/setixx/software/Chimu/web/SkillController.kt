package setixx.software.Chimu.web

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import setixx.software.Chimu.dto.SkillResponse
import setixx.software.Chimu.repository.SkillRepository

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