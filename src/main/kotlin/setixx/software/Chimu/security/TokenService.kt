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
import java.util.UUID

@Component
class TokenService(
    @Value("\${JWT_SECRET}") private val secret: String,
) {
    private val secretKey: Key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret))

    fun generateToken(
        publicId: UUID,
        expiration: Date,
        tokenType: String,
        additionalClaims: Map<String, Any> = emptyMap()
    ): String {
        val claims = additionalClaims.toMutableMap()
        claims["type"] = tokenType

        return Jwts.builder()
            .setHeaderParam("typ", "JWT")
            .setClaims(claims)
            .setSubject(publicId.toString())
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(expiration)
            .signWith(secretKey)
            .compact()
    }

    fun extractPublicId(token: String): UUID {
        return UUID.fromString(extractAllClaims(token).subject)
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
            val publicId = extractPublicId(token)
            val claims = extractAllClaims(token)
            val customUserDetails = userDetails as? CustomUserDetails
            publicId == customUserDetails?.publicId && !isTokenExpired(claims)
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