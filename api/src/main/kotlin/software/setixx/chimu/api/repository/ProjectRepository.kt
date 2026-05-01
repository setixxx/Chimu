package software.setixx.chimu.api.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import software.setixx.chimu.api.domain.Project
import software.setixx.chimu.api.domain.ProjectStatus
import java.util.UUID

interface ProjectRepository : JpaRepository<Project, Long> {
    fun findByPublicIdAndDeletedAtIsNull(publicId: UUID): Project?
    fun findByTeamIdAndGameJamIdAndDeletedAtIsNull(teamId: Long, jamId: Long): Project?
    fun findAllByGameJamIdAndDeletedAtIsNull(jamId: Long): List<Project>
    fun findAllByTeamIdAndDeletedAtIsNull(teamId: Long): List<Project>

    @Query("""
        SELECT COUNT(p) FROM Project p 
        WHERE p.gameJam.id = :jamId 
        AND p.status IN ('SUBMITTED', 'UNDER_REVIEW', 'PUBLISHED')
        AND p.deletedAt IS NULL
    """)
    fun countSubmittedProjects(@Param("jamId") jamId: Long): Long

    fun findAllByGameJamIdAndStatusAndDeletedAtIsNull(jamId: Long, status: ProjectStatus): List<Project>

    @Query("""
        SELECT p FROM Project p 
        WHERE p.gameJam.id = :jamId 
        AND p.status IN ('PUBLISHED')
        AND p.deletedAt IS NULL
    """)
    fun findPublishedProjectsByJamId(@Param("jamId") jamId: Long): List<Project>

    @Query("SELECT p FROM Project p JOIN TeamMember tm ON p.team.id = tm.team.id WHERE tm.user.id = :userId AND p.deletedAt IS NULL AND tm.deletedAt IS NULL")
    fun findAllByUserId(@Param("userId") userId: Long): List<Project>

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Project p SET p.deletedAt = CURRENT_TIMESTAMP WHERE p.id = :id")
    fun softDeleteById(@Param("id") id: Long)
}