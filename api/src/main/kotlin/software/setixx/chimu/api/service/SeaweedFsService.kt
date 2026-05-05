package software.setixx.chimu.api.service

import org.slf4j.LoggerFactory
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import software.setixx.chimu.api.config.SeaweedFsProperties
import java.io.InputStream
import java.io.OutputStream

@Service
class SeaweedFsService(
    private val properties: SeaweedFsProperties
) {

    private val log = LoggerFactory.getLogger(SeaweedFsService::class.java)

    private val restTemplate: RestTemplate = RestTemplate(JdkClientHttpRequestFactory())

    fun upload(
        filePath: String,
        inputStream: InputStream,
        fileSize: Long,
        fileName: String,
        mimeType: String
    ) {
        val resource = object : InputStreamResource(inputStream) {
            override fun getFilename(): String = fileName
            override fun contentLength(): Long = fileSize
        }

        val body = LinkedMultiValueMap<String, Any>()
        body.add("file", resource)

        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA

        restTemplate.postForEntity(
            buildUrl(filePath),
            HttpEntity(body, headers),
            String::class.java
        )
    }

    fun streamToOutput(filePath: String, outputStream: OutputStream) {
        restTemplate.execute(
            buildUrl(filePath),
            HttpMethod.GET,
            null,
            { response -> response.body.use { source -> source.copyTo(outputStream) } }
        )
    }

    fun resolve(filePath: String) {
        restTemplate.execute(
            buildUrl(filePath),
            HttpMethod.HEAD,
            null,
            { response -> response }
        )
    }

    fun delete(filePath: String) {
        try {
            restTemplate.delete(buildUrl(filePath))
        } catch (ex: Exception) {
            log.warn("Failed to delete file from SeaweedFS filer [path={}]: {}", filePath, ex.message)
        }
    }

    private fun buildUrl(filePath: String): String =
        "${properties.filerUrl.trimEnd('/')}/${filePath.trimStart('/')}"
}