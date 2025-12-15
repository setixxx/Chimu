package setixx.software.Chimu.web

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import setixx.software.Chimu.dto.SpecializationResponse
import setixx.software.Chimu.service.SpecializationService

@RestController
@RequestMapping("/api/specializations")
class SpecializationController(
    private val specializationService: SpecializationService
) {
    @GetMapping
    fun getAllSpecializations(): ResponseEntity<List<SpecializationResponse>> {
        val specializations = specializationService.getAllSpecializations()
        return ResponseEntity.ok(specializations)
    }
}