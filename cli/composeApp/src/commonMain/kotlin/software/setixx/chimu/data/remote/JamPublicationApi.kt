package software.setixx.chimu.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import software.setixx.chimu.data.remote.dto.GameJamDetailsResponse
import software.setixx.chimu.domain.model.FileUpload

/**
 * Класс для управления процессом публикации джема и загрузки баннеров.
 */
class JamPublicationApi(private val client: HttpClient) {

    suspend fun publishJam(jamId: String, accessToken: String): GameJamDetailsResponse {
        val response = client.post("/api/jams/$jamId/publish") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            header(JAM_ID_HEADER, jamId)
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            400 -> throw IllegalArgumentException("Заполните критерии, судей и баннер джема")
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            403 -> throw IllegalArgumentException("Недостаточно прав для публикации джема")
            404 -> throw IllegalArgumentException("Джем не найден")
            409 -> throw IllegalArgumentException("Джем уже опубликован или не может быть опубликован")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }

    suspend fun uploadBanner(jamId: String, accessToken: String, file: FileUpload) {
        val response = client.post("/api/jams/$jamId/banner") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            header(JAM_ID_HEADER, jamId)
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append(
                            key = "file",
                            value = file.bytes,
                            headers = Headers.build {
                                append(HttpHeaders.ContentType, file.mimeType)
                                append(
                                    HttpHeaders.ContentDisposition,
                                    "filename=\"${file.fileName}\""
                                )
                            }
                        )
                    }
                )
            )
        }
        when (response.status.value) {
            in 200..299 -> return
            400 -> throw IllegalArgumentException("Баннер должен быть изображением png, webp или jpg")
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            403 -> throw IllegalArgumentException("Недостаточно прав")
            404 -> throw IllegalArgumentException("Джем не найден")
            413 -> throw IllegalArgumentException("Файл слишком большой")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }

    suspend fun deleteBanner(jamId: String, accessToken: String) {
        val response = client.delete("/api/jams/$jamId/banner") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            header(JAM_ID_HEADER, jamId)
        }
        when (response.status.value) {
            in 200..299 -> return
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            403 -> throw IllegalArgumentException("Недостаточно прав")
            404 -> throw IllegalArgumentException("Баннер не найден")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }

    private companion object {
        const val JAM_ID_HEADER = "jamId"
    }
}
