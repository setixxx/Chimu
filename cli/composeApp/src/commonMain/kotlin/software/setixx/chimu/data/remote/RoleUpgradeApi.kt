package software.setixx.chimu.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import software.setixx.chimu.data.remote.dto.CreateRoleUpgradeRequest
import software.setixx.chimu.data.remote.dto.ReviewRoleUpgradeRequest
import software.setixx.chimu.data.remote.dto.RoleUpgradeResponse

class RoleUpgradeApi(
    private val client: HttpClient
) {
    suspend fun getUserRoleUpgrades(
        accessToken: String
    ): List<RoleUpgradeResponse> {
        val response = client.get("/api/users/me/role-requests") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            403 -> throw IllegalArgumentException("Недостаточно прав для получения списка заявок на роль")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }

    suspend fun createRoleUpgrade(
        data: CreateRoleUpgradeRequest,
        accessToken: String
    ): RoleUpgradeResponse {
        val response = client.post("/api/users/me/role-requests") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            setBody(data)
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            403 -> throw IllegalArgumentException("Недостаточно прав для создания заявки на роль")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }

    suspend fun reviewRoleUpgrade(
        requestId: String,
        data: ReviewRoleUpgradeRequest,
        accessToken: String
    ): RoleUpgradeResponse {
        val response = client.patch("/api/admin/role-requests/$requestId") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            setBody(data)
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            403 -> throw IllegalArgumentException("Недостаточно прав для рассмотрения заявки на роль")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }

    suspend fun getAllRoleUpgrades(
        accessToken: String
    ): List<RoleUpgradeResponse> {
        val response = client.get("/api/admin/role-requests") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            403 -> throw IllegalArgumentException("Недостаточно прав для получения списка заявок на роль")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }

    suspend fun cancelRoleUpgrade(
        requestId: String,
        accessToken: String
    ): RoleUpgradeResponse {
        val response = client.delete("/api/users/me/role-requests/$requestId") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        when (response.status.value) {
            in 200..299 -> return response.body()
            401 -> throw IllegalArgumentException("Ошибка авторизации")
            403 -> throw IllegalArgumentException("Недостаточно прав для отмены заявки на роль")
            else -> throw IllegalArgumentException("Неизвестная ошибка")
        }
    }
}