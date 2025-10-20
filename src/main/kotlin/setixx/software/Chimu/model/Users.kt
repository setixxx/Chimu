package setixx.software.Chimu.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "users")
data class Users(
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    val id : UUID = UUID.randomUUID(),

    @Column(nullable = false, unique = true, columnDefinition = "citext")
    val email : String,

    @Column(name = "password_hash", nullable = false)
    val passwordHash : String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role : UsersRole,

    @Column(name = "display_name", length = 120, nullable = false)
    val displayName : String,

    @Column(name = "avatar_url")
    val avatarUrl : String?,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
)
