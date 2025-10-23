package setixx.software.Chimu.repository

import org.springframework.data.jpa.repository.JpaRepository
import setixx.software.Chimu.model.User
import java.util.UUID

interface UserRepository : JpaRepository<User, UUID> {
    fun findByPublicId(publicId: UUID): User?
}