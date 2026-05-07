package software.setixx.chimu.domain.usecase

import software.setixx.chimu.domain.model.FileUpload
import software.setixx.chimu.domain.repository.JamPublicationRepository

class PublishJamUseCase(
    private val repository: JamPublicationRepository
) {
    suspend operator fun invoke(jamId: String) = repository.publishJam(jamId)
}

class GetJamBannerUseCase(
    private val repository: JamPublicationRepository
) {
    suspend operator fun invoke(jamId: String) = repository.getBanner(jamId)
}

class UploadJamBannerUseCase(
    private val repository: JamPublicationRepository
) {
    suspend operator fun invoke(jamId: String, file: FileUpload) = repository.uploadBanner(jamId, file)
}

class DeleteJamBannerUseCase(
    private val repository: JamPublicationRepository
) {
    suspend operator fun invoke(jamId: String) = repository.deleteBanner(jamId)
}
