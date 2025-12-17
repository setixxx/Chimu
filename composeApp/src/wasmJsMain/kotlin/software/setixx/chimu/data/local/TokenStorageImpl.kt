package software.setixx.chimu.data.local

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.browser.window

actual fun createTokenStorage(): TokenStorage = WasmJsTokenStorage()

class WasmJsTokenStorage : TokenStorage {
    private val localStorage = window.localStorage

    override suspend fun saveAccessToken(token: String) = withContext(Dispatchers.Default) {
        localStorage.setItem(KEY_ACCESS_TOKEN, token)
    }

    override suspend fun getAccessToken(): String? = withContext(Dispatchers.Default) {
        localStorage.getItem(KEY_ACCESS_TOKEN)
    }

    override suspend fun saveRefreshToken(token: String) = withContext(Dispatchers.Default) {
        localStorage.setItem(KEY_REFRESH_TOKEN, token)
    }

    override suspend fun getRefreshToken(): String? = withContext(Dispatchers.Default) {
        localStorage.getItem(KEY_REFRESH_TOKEN)
    }

    override suspend fun saveEmail(email: String) = withContext(Dispatchers.Default) {
        localStorage.setItem(KEY_EMAIL, email)
    }

    override suspend fun getEmail(): String? = withContext(Dispatchers.Default) {
        localStorage.getItem(KEY_EMAIL)
    }

    override suspend fun clearTokens() = withContext(Dispatchers.Default) {
        localStorage.removeItem(KEY_ACCESS_TOKEN)
        localStorage.removeItem(KEY_REFRESH_TOKEN)
        localStorage.removeItem(KEY_EMAIL)
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "chimu_access_token"
        private const val KEY_REFRESH_TOKEN = "chimu_refresh_token"
        private const val KEY_EMAIL = "chimu_email"
    }
}