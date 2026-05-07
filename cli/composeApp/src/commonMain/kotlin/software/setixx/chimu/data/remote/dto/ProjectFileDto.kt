package software.setixx.chimu.data.remote.dto

import kotlinx.serialization.Serializable
import software.setixx.chimu.api.domain.ProjectFileType

@Serializable
data class ProjectFileResponse(
    val id: String,
    val fileName: String,
    val fileSize: Long,
    val mimeType: String,
    val fileType: ProjectFileType,
    val uploadedAt: String,
    val uploadedByUserId: String
)