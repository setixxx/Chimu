package software.setixx.chimu.api.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import software.setixx.chimu.api.domain.Project
import software.setixx.chimu.api.domain.ProjectStatus
import java.util.UUID

interface ProjectRepository : JpaRepository<Project, Long> {
    fun findByPublicId(publicId: UUID): Project?

    fun findByTeamIdAndJamId(teamId: Long, jamId: Long): Project?

    fun findAllByJamId(jamId: Long): List<Project>

    fun findAllByTeamId(teamId: Long): List<Project>

    @Query("""
        SELECT COUNT(p) FROM Project p 
        WHERE p.jamId = :jamId 
        AND p.status IN ('SUBMITTED', 'UNDER_REVIEW', 'PUBLISHED')
    """)
    fun countSubmittedProjects(@Param("jamId") jamId: Long): Long

    @Query("""
        SELECT p FROM Project p 
        WHERE p.jamId = :jamId 
        AND p.status = :status
    """)
    fun findAllByJamIdAndStatus(
        @Param("jamId") jamId: Long,
        @Param("status") status: ProjectStatus
    ): List<Project>
}