package software.setixx.chimu.domain.usecase

import software.setixx.chimu.domain.model.GameJam
import software.setixx.chimu.domain.repository.GameJamRepository

class GetActiveJamsUseCase(
    private val repository: GameJamRepository
) {
    suspend operator fun invoke() = repository.getActiveJams()
}

class GetAllJamsUseCase(
    private val repository: GameJamRepository
) {
    suspend operator fun invoke() = repository.getAllJams()
}