package software.setixx.chimu.domain.usecase

import software.setixx.chimu.domain.model.AssignJudge
import software.setixx.chimu.domain.repository.JudgeRepository

class GetJamJudgesUseCase(
    private val repository: JudgeRepository
) {
    suspend operator fun invoke(jamId: String) = repository.getJamJudges(jamId)
}

class AssignJudgeUseCase(
    private val repository: JudgeRepository
){
    suspend operator fun invoke(jamId: String, data: AssignJudge) = repository.assignJudge(jamId, data)
}

class UnassignJudgeUseCase(
    private val repository: JudgeRepository
) {
    suspend operator fun invoke(jamId: String, judgeUserId: String) =
        repository.unassignJudge(jamId, judgeUserId)
}