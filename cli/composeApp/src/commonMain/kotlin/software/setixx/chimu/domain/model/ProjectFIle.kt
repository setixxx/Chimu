package software.setixx.chimu.domain.model

import software.setixx.chimu.api.domain.ProjectFileType

data class ProjectFile(
    val id: String,
    val fileName: String,
    val fileSize: Long,
    val mimeType: String,
    val fileType: ProjectFileType,
    val uploadedAt: String,
    val uploadedByUserId: String
)