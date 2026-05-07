package software.setixx.chimu.data.repository

import software.setixx.chimu.api.domain.GameJamStatus
import software.setixx.chimu.data.local.TokenStorage
import software.setixx.chimu.data.remote.JamPublicationApi
import software.setixx.chimu.data.remote.dto.GameJamDetailsResponse
import software.setixx.chimu.data.remote.dto.JamBannerResponse
import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.model.FileUpload
import software.setixx.chimu.domain.model.GameJamDetails
import software.setixx.chimu.domain.model.JamBanner
import software.setixx.chimu.domain.repository.JamPublicationRepository

class JamPublicationRepositoryImpl(
    private val api: JamPublicationApi,
    private val tokenStorage: TokenStorage
) : JamPublicationRepository {

    override suspend fun publishJam(jamId: String): ApiResult<GameJamDetails> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")

            val response = api.publishJam(jamId, token)
            ApiResult.Success(response.toDomain())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    override suspend fun getBanner(jamId: String): ApiResult<JamBanner> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")

            val response = api.getBanner(jamId, token)
            ApiResult.Success(response.toDomain())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    override suspend fun uploadBanner(jamId: String, file: FileUpload): ApiResult<Unit> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")

            if (!file.isAllowedBannerImage()) {
                return ApiResult.Error("Баннер должен быть изображением png, webp или jpg")
            }

            api.uploadBanner(jamId, token, file)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    override suspend fun deleteBanner(jamId: String): ApiResult<Unit> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return ApiResult.Error("Ошибка аутентификации")

            api.deleteBanner(jamId, token)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подключения к серверу")
        }
    }

    private fun FileUpload.isAllowedBannerImage(): Boolean {
        val normalizedMime = mimeType.substringBefore(";").trim().lowercase()
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return normalizedMime in ALLOWED_IMAGE_MIME_TYPES || extension in ALLOWED_IMAGE_EXTENSIONS
    }

    private fun JamBannerResponse.toDomain(): JamBanner {
        return JamBanner(
            bytes = bytes,
            mimeType = mimeType,
            fileName = fileName
        )
    }

    private fun GameJamDetailsResponse.toDomain(): GameJamDetails {
        return GameJamDetails(
            id = id,
            name = name,
            description = description,
            theme = theme,
            rules = rules,
            registrationStart = registrationStart,
            registrationEnd = registrationEnd,
            jamStart = jamStart,
            jamEnd = jamEnd,
            judgingStart = judgingStart,
            judgingEnd = judgingEnd,
            status = GameJamStatus.valueOf(status),
            organizerId = organizerId,
            organizerNickname = organizerNickname,
            minTeamSize = minTeamSize,
            maxTeamSize = maxTeamSize,
            createdAt = createdAt,
            updatedAt = updatedAt,
            criteria = criteria,
            judges = judges,
            registeredTeamsCount = registeredTeamsCount,
            submittedProjectsCount = submittedProjectsCount
        )
    }

    private companion object {
        val ALLOWED_IMAGE_MIME_TYPES = setOf("image/png", "image/webp", "image/jpeg", "image/jpg")
        val ALLOWED_IMAGE_EXTENSIONS = setOf("png", "webp", "jpg", "jpeg")
    }
}
