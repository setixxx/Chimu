package software.setixx.chimu.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual fun createTokenStorage(): TokenStorage = AndroidTokenStorageProvider.get()

object AndroidTokenStorageProvider {
    private var instance: TokenStorage? = null

    fun init(context: Context) {
        if (instance == null) {
            instance = AndroidTokenStorage(context)
        }
    }

    fun get(): TokenStorage {
        return instance ?: throw IllegalStateException("TokenStorage not initialized. Call init(context) first")
    }
}

class AndroidTokenStorage(context: Context) : TokenStorage {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "chimu_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override suspend fun saveAccessToken(token: String) = withContext(Dispatchers.IO) {
        sharedPreferences.edit { putString(KEY_ACCESS_TOKEN, token) }
    }

    override suspend fun getAccessToken(): String? = withContext(Dispatchers.IO) {
        sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
    }

    override suspend fun saveRefreshToken(token: String) = withContext(Dispatchers.IO) {
        sharedPreferences.edit { putString(KEY_REFRESH_TOKEN, token) }
    }

    override suspend fun getRefreshToken(): String? = withContext(Dispatchers.IO) {
        sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
    }

    override suspend fun saveEmail(email: String) = withContext(Dispatchers.IO) {
        sharedPreferences.edit { putString(KEY_EMAIL, email) }
    }

    override suspend fun getEmail(): String? = withContext(Dispatchers.IO) {
        sharedPreferences.getString(KEY_EMAIL, null)
    }

    override suspend fun clearTokens() = withContext(Dispatchers.IO) {
        sharedPreferences.edit { clear() }
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "chimu_access_token"
        private const val KEY_REFRESH_TOKEN = "chimu_refresh_token"
        private const val KEY_EMAIL = "chimu_email"
    }
}