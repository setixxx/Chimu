package software.setixx.chimu.api.repository

import org.springframework.data.jpa.repository.JpaRepository
import software.setixx.chimu.api.domain.User
import java.util.UUID

interface UserRepository : JpaRepository<User, Long> {
    fun findByPublicId(publicId: UUID): User?

    fun findByEmail(email: String): User?

    fun findByNickname(nickname: String): User?
}