package software.setixx.chimu.api.dto

import software.setixx.chimu.api.domain.ProjectFileType


data class ProjectFileResponse(
    val id: String,
    val fileName: String,
    val fileSize: Long,
    val mimeType: String,
    val fileType: ProjectFileType,
    val uploadedAt: String,
    val uploadedByUserId: String
)

data class ProjectFileDownloadMeta(
    val id: Long,
    val filePath: String,
    val fileName: String,
    val fileSize: Long,
    val mimeType: String
)