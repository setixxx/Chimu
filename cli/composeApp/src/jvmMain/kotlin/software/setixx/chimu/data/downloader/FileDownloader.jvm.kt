package software.setixx.chimu.data.downloader

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.swing.JFileChooser

actual class FileDownloader {
    actual suspend fun save(fileName: String, bytes: ByteArray, mimeType: String) {
        withContext(Dispatchers.IO) {
            val chooser = JFileChooser()
            chooser.dialogType = JFileChooser.SAVE_DIALOG
            chooser.selectedFile = File(fileName)
            val result = chooser.showSaveDialog(null)
            if (result == JFileChooser.APPROVE_OPTION) {
                chooser.selectedFile.writeBytes(bytes)
            }
        }
    }
}