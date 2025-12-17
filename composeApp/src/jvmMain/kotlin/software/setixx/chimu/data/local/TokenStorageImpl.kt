package software.setixx.chimu.data.local

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.prefs.Preferences

actual fun createTokenStorage(): TokenStorage = JvmTokenStorage()

class JvmTokenStorage : TokenStorage {
    private val prefs = Preferences.userNodeForPackage(JvmTokenStorage::class.java)

    override suspend fun saveAccessToken(token: String) = withContext(Dispatchers.IO) {
        prefs.put(KEY_ACCESS_TOKEN, token)
    }

    override suspend fun getAccessToken(): String? = withContext(Dispatchers.IO) {
        prefs.get(KEY_ACCESS_TOKEN, null)
    }

    override suspend fun saveRefreshToken(token: String) = withContext(Dispatchers.IO) {
        prefs.put(KEY_REFRESH_TOKEN, token)
    }

    override suspend fun getRefreshToken(): String? = withContext(Dispatchers.IO) {
        prefs.get(KEY_REFRESH_TOKEN, null)
    }

    override suspend fun saveEmail(email: String) = withContext(Dispatchers.IO) {
        prefs.put(KEY_EMAIL, email)
    }

    override suspend fun getEmail(): String? = withContext(Dispatchers.IO) {
        prefs.get(KEY_EMAIL, null)
    }

    override suspend fun clearTokens() = withContext(Dispatchers.IO) {
        prefs.remove(KEY_ACCESS_TOKEN)
        prefs.remove(KEY_REFRESH_TOKEN)
        prefs.remove(KEY_EMAIL)
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "chimu_access_token"
        private const val KEY_REFRESH_TOKEN = "chimu_refresh_token"
        private const val KEY_EMAIL = "chimu_email"
    }
}