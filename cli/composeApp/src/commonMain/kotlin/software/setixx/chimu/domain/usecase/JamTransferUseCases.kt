package software.setixx.chimu.domain.usecase

import software.setixx.chimu.domain.model.CreateJamTransfer
import software.setixx.chimu.domain.model.ReviewJamTransfer
import software.setixx.chimu.domain.repository.JamTransferRepository

class CreateTransferUseCase(
    private val repository: JamTransferRepository
){
    suspend operator fun invoke(jamId: String, data: CreateJamTransfer) =
        repository.createTransferRequest(jamId, data)
}

class CancelTransferUseCase(
    private val repository: JamTransferRepository
){
    suspend operator fun invoke(jamId: String) = repository.cancelTransferRequest(jamId)
}

class ReviewTransferUseCase(
    private val repository: JamTransferRepository
) {
    suspend operator fun invoke(requestId: String, data: ReviewJamTransfer) =
        repository.reviewTransferRequest(requestId, data)
}

class GetTransferRequestsUseCase(
    private val repository: JamTransferRepository
) {
    suspend operator fun invoke() = repository.getTransferRequests()
}