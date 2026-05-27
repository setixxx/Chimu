package software.setixx.chimu.data.downloader

actual class FileDownloader {
    actual suspend fun save(
        fileName: String,
        bytes: ByteArray,
        mimeType: String
    ) {
    }
}