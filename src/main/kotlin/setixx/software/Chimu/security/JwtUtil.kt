package setixx.software.Chimu.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.Key
import java.util.Date

@Component
class JwtUtil(
    @Value("\${JWT_SECRET}") private val secret: String,
    @Value("\${JWT_EXPIRATION}") private val expirationTime: Long,
){
    private val secretKey: Key = Keys.hmacShaKeyFor(secret.toByteArray())

    fun generateToken(username: String): String {
        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + expirationTime))
            .signWith(secretKey)
            .compact()
    }

    fun validateToken(token: String): Boolean {
        return try {
            true
        } catch (e: Exception) {
            false
        }
    }
}