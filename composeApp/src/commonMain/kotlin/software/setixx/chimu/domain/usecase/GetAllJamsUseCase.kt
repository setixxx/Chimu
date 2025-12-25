package software.setixx.chimu.domain.usecase

import software.setixx.chimu.domain.model.GameJam
import software.setixx.chimu.domain.repository.GameJamRepository

class GetAllJamsUseCase(
    private val repository: GameJamRepository
) {
    suspend operator fun invoke(): Result<List<GameJam>> {
        return repository.getAllJams()
    }
}