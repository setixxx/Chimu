package software.setixx.chimu.domain.usecase

import software.setixx.chimu.domain.model.CreateGameJam
import software.setixx.chimu.domain.model.GameJam
import software.setixx.chimu.domain.model.UpdateGameJam
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

class CreateJamUseCase(
    private val repository: GameJamRepository
) {
    suspend operator fun invoke(data: CreateGameJam) = repository.createJam(data)
}

class GetJamDetailsUseCase(
    private val repository: GameJamRepository
) {
    suspend operator fun invoke(gameJamId: String) = repository.getJamDetails(gameJamId)
}

class DeleteJamUseCase(
    private val repository: GameJamRepository
) {
    suspend operator fun invoke(gameJamId: String) = repository.deleteJam(gameJamId)
}

class UpdateJamUseCase(
    private val repository: GameJamRepository
) {
    suspend operator fun invoke(gameJamId: String, data: UpdateGameJam) = repository.updateJam(gameJamId, data)
}