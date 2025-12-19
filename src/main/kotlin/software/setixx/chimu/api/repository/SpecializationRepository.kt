package software.setixx.chimu.api.repository

import org.springframework.data.jpa.repository.JpaRepository
import software.setixx.chimu.api.domain.Specialization

interface SpecializationRepository : JpaRepository<Specialization, Long> {
    fun findByName(name: String): Specialization?
}