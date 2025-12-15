package setixx.software.Chimu.repository

import org.springframework.data.jpa.repository.JpaRepository
import setixx.software.Chimu.domain.Specialization

interface SpecializationRepository : JpaRepository<Specialization, Long> {
    fun findByName(name: String): Specialization?
}