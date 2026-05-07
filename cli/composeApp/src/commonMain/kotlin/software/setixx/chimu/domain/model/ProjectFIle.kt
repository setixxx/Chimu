package software.setixx.chimu.domain.model

import software.setixx.chimu.api.domain.ProjectFileType

data class FileUpload(
    val fileName: String,
    val bytes: ByteArray,
    val mimeType: String,
    val fileType: ProjectFileType
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as FileUpload

        if (fileName != other.fileName) return false
        if (!bytes.contentEquals(other.bytes)) return false
        if (mimeType != other.mimeType) return false
        if (fileType != other.fileType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fileName.hashCode()
        result = 31 * result + bytes.contentHashCode()
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + fileType.hashCode()
        return result
    }
}

data class ProjectFile(
    val id: String,
    val fileName: String,
    val fileSize: Long,
    val mimeType: String,
    val fileType: ProjectFileType,
    val uploadedAt: String,
    val uploadedByUserId: String,
    val bytes: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as ProjectFile
        if (id != other.id) return false
        if (bytes != null && other.bytes != null && !bytes.contentEquals(other.bytes)) return false
        return fileName == other.fileName && fileSize == other.fileSize
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + fileName.hashCode()
        result = 31 * result + fileSize.hashCode()
        result = 31 * result + (bytes?.contentHashCode() ?: 0)
        return result
    }
}