package software.setixx.chimu.api.domain

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "projects",
    uniqueConstraints = [
        UniqueConstraint(name = "uq_projects_public_id", columnNames = ["public_id"]),
        UniqueConstraint(name = "uq_projects_team_jam", columnNames = ["team_id", "jam_id"])
    ],
    indexes = [
        Index(name = "idx_projects_team_id", columnList = "team_id"),
        Index(name = "idx_projects_jam_id", columnList = "jam_id"),
        Index(name = "idx_projects_status", columnList = "status"),
        Index(name = "idx_projects_submitted_at", columnList = "submitted_at"),
        Index(name = "idx_projects_created_at", columnList = "created_at")
    ]
)
class Project(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "public_id", nullable = false, columnDefinition = "uuid")
    var publicId: UUID = UUID.randomUUID(),

    @Column(name = "team_id")
    var teamId: Long?,

    @Column(name = "jam_id", nullable = false)
    var jamId: Long,

    @Column(nullable = false, length = 200)
    var title: String,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(name = "game_url", columnDefinition = "TEXT")
    var gameUrl: String? = null,

    @Column(name = "repository_url", columnDefinition = "TEXT")
    var repositoryUrl: String? = null,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "project_status")
    var status: ProjectStatus = ProjectStatus.DRAFT,

    @Version
    @Column(nullable = false)
    var version: Long? = null,

    @Column(name = "submitted_at")
    var submittedAt: Instant? = null,

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    var createdAt: Instant? = null,

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    var updatedAt: Instant? = null
)

enum class ProjectStatus {
    DRAFT,
    SUBMITTED,
    UNDER_REVIEW,
    PUBLISHED,
    DISQUALIFIED
}