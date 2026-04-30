package software.setixx.chimu.api.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Generated
import org.hibernate.generator.EventType
import java.time.Instant

@Entity
@Table(name = "project_files")
class ProjectFile(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    val project: Project,

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false)
    var fileType: ProjectFileType,

    @Column(name = "file_url", nullable = false, columnDefinition = "TEXT")
    var fileUrl: String,

    @Column(name = "file_name", nullable = false, length = 255)
    var fileName: String,

    @Column(name = "file_size", nullable = false)
    var fileSize: Long,

    @Column(name = "mime_type", nullable = false, length = 100)
    var mimeType: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    val uploadedBy: User,

    @Generated(event = [EventType.INSERT])
    @Column(name = "uploaded_at", nullable = false, updatable = false, insertable = false)
    val uploadedAt: Instant? = null,

    @Column(name = "deleted_at")
    var deletedAt: Instant? = null
)