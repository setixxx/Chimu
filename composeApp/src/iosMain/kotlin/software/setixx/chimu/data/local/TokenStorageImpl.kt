package software.setixx.chimu.data.local

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSUserDefaults

actual fun createTokenStorage(): TokenStorage = IosTokenStorage()

class IosTokenStorage : TokenStorage {
    private val userDefaults = NSUserDefaults.standardUserDefaults

    override suspend fun saveAccessToken(token: String) = withContext(Dispatchers.Default) {
        userDefaults.setObject(token, forKey = KEY_ACCESS_TOKEN)
    }

    override suspend fun getAccessToken(): String? = withContext(Dispatchers.Default) {
        userDefaults.stringForKey(KEY_ACCESS_TOKEN)
    }

    override suspend fun saveRefreshToken(token: String) = withContext(Dispatchers.Default) {
        userDefaults.setObject(token, forKey = KEY_REFRESH_TOKEN)
    }

    override suspend fun getRefreshToken(): String? = withContext(Dispatchers.Default) {
        userDefaults.stringForKey(KEY_REFRESH_TOKEN)
    }

    override suspend fun saveEmail(email: String) = withContext(Dispatchers.Default) {
        userDefaults.setObject(email, forKey = KEY_EMAIL)
    }

    override suspend fun getEmail(): String? = withContext(Dispatchers.Default) {
        userDefaults.stringForKey(KEY_EMAIL)
    }

    override suspend fun clearTokens() = withContext(Dispatchers.Default) {
        userDefaults.removeObjectForKey(KEY_ACCESS_TOKEN)
        userDefaults.removeObjectForKey(KEY_REFRESH_TOKEN)
        userDefaults.removeObjectForKey(KEY_EMAIL)
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "chimu_access_token"
        private const val KEY_REFRESH_TOKEN = "chimu_refresh_token"
        private const val KEY_EMAIL = "chimu_email"
    }
}