package software.setixx.chimu.api.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import software.setixx.chimu.api.domain.ProjectFile
import software.setixx.chimu.api.domain.ProjectFileType
import java.util.UUID

interface ProjectFileRepository : JpaRepository<ProjectFile, Long> {

    fun findAllByProjectIdAndDeletedAtIsNull(projectId: Long): List<ProjectFile>

    fun findByIdAndProjectIdAndDeletedAtIsNull(id: Long, projectId: Long): ProjectFile?

    fun countByProjectIdAndFileTypeAndDeletedAtIsNull(
        projectId: Long,
        fileType: ProjectFileType
    ): Long

    fun findByPublicIdAndDeletedAtIsNull(publicId: UUID): ProjectFile?

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE ProjectFile pf SET pf.deletedAt = CURRENT_TIMESTAMP WHERE pf.id = :id AND pf.deletedAt IS NULL")
    fun softDeleteById(@Param("id") id: Long)
}