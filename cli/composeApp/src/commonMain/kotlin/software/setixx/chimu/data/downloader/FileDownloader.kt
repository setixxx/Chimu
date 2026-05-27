package software.setixx.chimu.data.downloader

expect class FileDownloader {
    suspend fun save(fileName: String, bytes: ByteArray, mimeType: String)
}