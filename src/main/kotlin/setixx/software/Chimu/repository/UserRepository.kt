package setixx.software.Chimu.repository

import org.springframework.data.jpa.repository.JpaRepository
import setixx.software.Chimu.model.Users
import java.util.UUID

interface UserRepository : JpaRepository<Users, UUID> {
}