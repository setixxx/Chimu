package setixx.software.Chimu.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.Key
import java.util.Date

@Component
class TokenService(
    @Value("\${JWT_SECRET}") private val secret: String,
){
    private val secretKey: Key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret))

    fun generateToken(email: String, expiration: Date, additionalClaims: Map<String, Any> = emptyMap()): String {
        return Jwts.builder()
            .setClaims(additionalClaims)
            .setSubject(email)
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(expiration)
            .signWith(secretKey)
            .compact()
    }

    fun extractUsername(token: String): String {
        return extractAllClaims(token).subject
    }

    private fun extractAllClaims(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .body
    }
}