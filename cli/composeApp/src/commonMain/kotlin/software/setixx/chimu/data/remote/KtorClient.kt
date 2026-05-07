package software.setixx.chimu.data.remote

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.request.HttpRequestPipeline
import io.ktor.serialization.kotlinx.json.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import software.setixx.chimu.data.local.TokenStorage
import software.setixx.chimu.getPlatform

class KtorClient(
    private val tokenStorage: TokenStorage,
) {
    val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = true
            })
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
/*            url {
                protocol = URLProtocol.HTTP
                host = "localhost"
                port = 8080
            }*/
            contentType(ContentType.Application.Json)
        }
    }

    suspend fun addAuthToken(client: HttpClient) {
        val accessToken = tokenStorage.getAccessToken()
        if (accessToken != null) {
            client.requestPipeline.intercept(HttpRequestPipeline.State) {
                context.headers.append(HttpHeaders.Authorization, "Bearer $accessToken")
            }
        }
/*        install(Auth){
            bearer {
                loadTokens {
                    val access = tokenStorage.getAccessToken() ?: return@loadTokens null
                    val refresh = tokenStorage.getRefreshToken() ?: return@loadTokens null
                    BearerTokens(accessToken = access, refreshToken = refresh)
                }

                refreshTokens {
                    val response = client.post("/api/auth/refresh") {
                        markAsRefreshTokenRequest()
                        header(HttpHeaders.Authorization, "Bearer ${oldTokens?.accessToken}")
                        setBody(RefreshTokenRequest(oldTokens?.refreshToken ?: ""))
                    }

                    if (response.status == HttpStatusCode.OK) {
                        val newTokens = response.body<TokenResponse>()
                        tokenStorage.saveAccessToken(newTokens.accessToken)
                        tokenStorage.saveRefreshToken(newTokens.refreshToken)
                        BearerTokens(newTokens.accessToken, newTokens.refreshToken)
                    } else {
                        null
                    }
                }
            }
        }*/

    }
}