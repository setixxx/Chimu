package software.setixx.chimu.domain.usecase

import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.model.Skill
import software.setixx.chimu.domain.repository.SkillRepository

class GetAllSkillsUseCase(
    private val repository: SkillRepository
) {
    suspend operator fun invoke() = repository.getAllSkills()
}