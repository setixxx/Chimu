package software.setixx.chimu.api.repository

import org.springframework.data.jpa.repository.JpaRepository
import software.setixx.chimu.api.domain.Specialization
import java.util.UUID

interface SpecializationRepository : JpaRepository<Specialization, Long> {
    fun findByName(name: String): Specialization?

    fun findByPublicId(publicId: UUID): Specialization?
}