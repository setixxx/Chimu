package software.setixx.chimu.data.downloader

import kotlinx.browser.document
import org.khronos.webgl.Uint8Array
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import kotlin.collections.forEachIndexed

actual class FileDownloader {
    actual suspend fun save(fileName: String, bytes: ByteArray, mimeType: String) {
        val uint8 = Uint8Array(bytes.size)
        bytes.forEachIndexed { i, b -> uint8.asDynamic()[i] = b.toInt() and 0xFF }
        val blob = Blob(arrayOf(uint8), BlobPropertyBag(type = mimeType))
        val url = URL.createObjectURL(blob)
        val anchor = document.createElement("a") as HTMLAnchorElement
        anchor.href = url
        anchor.download = fileName
        document.body?.appendChild(anchor)
        anchor.click()
        document.body?.removeChild(anchor)
        URL.revokeObjectURL(url)
    }
}