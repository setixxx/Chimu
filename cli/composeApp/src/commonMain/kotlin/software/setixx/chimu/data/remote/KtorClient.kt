package software.setixx.chimu.data.remote

import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.serialization.kotlinx.json.*
import io.ktor.http.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import software.setixx.chimu.data.local.TokenStorage
import software.setixx.chimu.data.remote.dto.RefreshTokenRequest
import software.setixx.chimu.data.remote.dto.TokenResponse
import software.setixx.chimu.data.util.getBaseUrl

/**
 * Класс для настройки и создания HTTP-клиента Ktor с поддержкой обновления токенов.
 */
class KtorClient(
    private val tokenStorage: TokenStorage,
) {
    private val refreshMutex = Mutex()
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = true
    }

    private val refreshClient = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 30_000
        }

        defaultRequest {
            url(getBaseUrl())
            contentType(ContentType.Application.Json)
        }
    }

    val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }

        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 30_000
        }

        defaultRequest {
            url(getBaseUrl())
            contentType(ContentType.Application.Json)
        }
    }.also { client ->
        client.plugin(HttpSend).intercept { request ->
            val originalCall = execute(request)
            if (originalCall.response.status != HttpStatusCode.Unauthorized || request.url.encodedPath.isAuthPath()) {
                return@intercept originalCall
            }

            val failedAccessToken = request.headers[HttpHeaders.Authorization]?.removePrefix("Bearer ")
            val refreshedAccessToken = refreshAccessToken(failedAccessToken)
                ?: return@intercept originalCall

            request.headers.remove(HttpHeaders.Authorization)
            request.headers.append(HttpHeaders.Authorization, "Bearer $refreshedAccessToken")
            execute(request)
        }
    }

    private suspend fun refreshAccessToken(failedAccessToken: String?): String? = refreshMutex.withLock {
        val currentAccessToken = tokenStorage.getAccessToken()
        if (currentAccessToken != null && currentAccessToken != failedAccessToken) {
            return@withLock currentAccessToken
        }

        val refreshToken = tokenStorage.getRefreshToken() ?: return@withLock null
        runCatching {
            val response = refreshClient.post("/api/auth/refresh") {
                setBody(RefreshTokenRequest(refreshToken))
            }

            if (response.status.value !in 200..299) {
                return@withLock null
            }

            val tokens = response.body<TokenResponse>()
            tokenStorage.saveAccessToken(tokens.accessToken)
            tokenStorage.saveRefreshToken(tokens.refreshToken)
            tokens.accessToken
        }.getOrNull()
    }

    private fun String.isAuthPath(): Boolean = startsWith("/api/auth")
}
