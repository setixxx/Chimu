package software.setixx.chimu.domain.usecase

import software.setixx.chimu.api.domain.GameJamStatus
import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.model.CreateGameJam
import software.setixx.chimu.domain.model.GameJamDetails
import software.setixx.chimu.domain.model.UpdateGameJam
import software.setixx.chimu.domain.repository.GameJamRepository

class ObserveJamsUseCase(
    private val repository: GameJamRepository
){
    operator fun invoke() = repository.allJams
}

class ObserveSelectedJamUseCase(
    private val repository: GameJamRepository
){
    operator fun invoke() = repository.selectedJam
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

class CancelJamUseCase(
    private val repository: GameJamRepository
){
    suspend operator fun invoke(gameId: String) = repository.cancelJam(gameId)
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

class ForceJamStatusUseCase(
    private val repository: GameJamRepository
){
    suspend operator fun invoke(jamId: String, targetStatus: GameJamStatus): ApiResult<GameJamDetails>{
        return repository.forceJamStatus(jamId, targetStatus)
    }
}