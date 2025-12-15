package setixx.software.Chimu.repository

import org.springframework.data.jpa.repository.JpaRepository
import setixx.software.Chimu.domain.User
import java.util.UUID

interface UserRepository : JpaRepository<User, Long> {
    fun findByPublicId(publicId: UUID): User?

    fun findByEmail(email: String): User?

    fun findByNickname(nickname: String): User?
}