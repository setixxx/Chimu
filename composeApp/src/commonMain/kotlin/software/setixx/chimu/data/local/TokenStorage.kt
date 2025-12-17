package software.setixx.chimu.data.local

interface TokenStorage {
    suspend fun saveAccessToken(token: String)
    suspend fun getAccessToken(): String?
    suspend fun saveRefreshToken(token: String)
    suspend fun getRefreshToken(): String?
    suspend fun saveEmail(email: String)
    suspend fun getEmail(): String?
    suspend fun clearTokens()
}

expect fun createTokenStorage(): TokenStorage