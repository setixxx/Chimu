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
import software.setixx.chimu.data.remote.dto.ProjectFileResponse
import software.setixx.chimu.domain.model.FileUpload
import software.setixx.chimu.domain.model.ProjectFile

class ProjectFileApi(private val client: HttpClient) {
    suspend fun getFiles(
        projectId: String,
        accessToken: String
    ): List<ProjectFileResponse> {
        val response = client.get("/api/projects/$projectId/files") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }

    suspend fun uploadFile(
        projectId: String,
        accessToken: String,
        file: FileUpload
    ): ProjectFileResponse {
        val response = client.post("/api/projects/$projectId/files") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
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
                        append("fileType", file.fileType.name)
                    }
                )
            )
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            403 -> throw IllegalArgumentException("Недостаточно прав")
            404 -> throw IllegalArgumentException("Проект не найден")
            413 -> throw IllegalArgumentException("Файл слишком большой")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }

    suspend fun downloadFile(
        projectId: String,
        fileId: String,
        accessToken: String
    ): ByteArray {
        val response = client.get("/api/projects/$projectId/files/$fileId") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            403 -> throw IllegalArgumentException("Недостаточно прав")
            404 -> throw IllegalArgumentException("Файл не найден")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }

    suspend fun deleteFile(
        projectId: String,
        fileId: String,
        accessToken: String
    ) {
        val response = client.delete("/api/projects/$projectId/files/$fileId") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        when (response.status.value) {
            in 200..299 -> return
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            403 -> throw IllegalArgumentException("Недостаточно прав")
            404 -> throw IllegalArgumentException("Файл не найден")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }

}