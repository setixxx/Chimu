package software.setixx.chimu.data.saver

import androidx.compose.runtime.Composable
import java.io.FileOutputStream
import javax.swing.JFileChooser

@Composable
actual fun rememberFileSaver(): (fileName: String, bytes: ByteArray, mimeType: String) -> Unit {
    return { fileName, bytes, _ ->
        val chooser = JFileChooser()
        chooser.selectedFile = java.io.File(fileName)
        val result = chooser.showSaveDialog(null)
        if (result == JFileChooser.APPROVE_OPTION) {
            FileOutputStream(chooser.selectedFile).use { it.write(bytes) }
        }
    }
}