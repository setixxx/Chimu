package software.setixx.chimu.data.downloader

/**
 * Платформенно-зависимый класс для сохранения файлов в файловую систему.
 */
expect class FileDownloader {
    suspend fun save(fileName: String, bytes: ByteArray, mimeType: String)
}