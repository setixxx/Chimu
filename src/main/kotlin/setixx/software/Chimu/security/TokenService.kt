package setixx.software.Chimu.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SignatureException
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.security.Key
import java.util.Date

@Component
class TokenService(
    @Value("\${JWT_SECRET}") private val secret: String,
){
    private val secretKey: Key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret))

    fun generateToken(
        email: String,
        expiration: Date,
        tokenType: String,
        additionalClaims: Map<String, Any> = emptyMap()
    ): String {
        val claims = additionalClaims.toMutableMap()
        claims["type"] = tokenType

        return Jwts.builder()
            .setClaims(claims)
            .setSubject(email)
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(expiration)
            .signWith(secretKey)
            .compact()
    }

    fun extractEmail(token: String): String {
        return extractAllClaims(token).subject
    }

    fun extractTokenType(token: String): String? {
        return try {
            extractAllClaims(token)["type"] as? String
        } catch (e: Exception) {
            null
        }
    }

    private fun extractAllClaims(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .body
    }

    fun isTokenValid(token: String, userDetails: UserDetails): Boolean {
        return try {
            val email = extractEmail(token)
            val claims = extractAllClaims(token)
            email == userDetails.username && !isTokenExpired(claims)
        } catch (e: ExpiredJwtException) {
            false
        } catch (e: MalformedJwtException) {
            false
        } catch (e: SignatureException) {
            false
        } catch (e: Exception) {
            false
        }
    }

    private fun isTokenExpired(claims: Claims): Boolean {
        return claims.expiration.before(Date())
    }
}