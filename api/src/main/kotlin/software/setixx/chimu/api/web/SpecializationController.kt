package software.setixx.chimu.api.web

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import software.setixx.chimu.api.dto.SpecializationResponse
import software.setixx.chimu.api.service.SpecializationService

@RestController
@RequestMapping("/api/specializations")
@Tag(name = "Specializations", description = "Specialization management")
class SpecializationController(
    private val specializationService: SpecializationService
) {
    @GetMapping
    @Operation(summary = "Get all specializations", description = "Retrieves a list of all available specializations")
    @ApiResponse(responseCode = "200", description = "Specializations retrieved successfully")
    fun getAllSpecializations(): ResponseEntity<List<SpecializationResponse>> {
        val specializations = specializationService.getAllSpecializations()
        return ResponseEntity.ok(specializations)
    }
}