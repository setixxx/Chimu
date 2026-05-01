package software.setixx.chimu.api.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import software.setixx.chimu.api.domain.User
import java.util.UUID

interface UserRepository : JpaRepository<User, Long> {
    fun findByPublicIdAndDeletedAtIsNull(publicId: UUID): User?
    fun findByEmailAndDeletedAtIsNull(email: String): User?
    fun findByNicknameAndDeletedAtIsNull(nickname: String): User?

    fun findByPublicId(publicId: UUID): User?
    fun findByEmail(email: String): User?
    fun findByNickname(nickname: String): User?

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE User u SET u.deletedAt = CURRENT_TIMESTAMP WHERE u.id = :id")
    fun softDeleteById(@Param("id") id: Long)
}