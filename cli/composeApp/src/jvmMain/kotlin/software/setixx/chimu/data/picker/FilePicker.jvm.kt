package software.setixx.chimu.data.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import software.setixx.chimu.api.domain.ProjectFileType
import software.setixx.chimu.domain.model.FileUpload
import software.setixx.chimu.domain.model.ProjectFile
import java.nio.file.Files
import javax.swing.JFileChooser

@Composable
actual fun rememberFilePicker(onResult: (FileUpload?) -> Unit): () -> Unit {
    val scope = rememberCoroutineScope()

    return {
        scope.launch(Dispatchers.IO) {
            val dialog = JFileChooser()
            val result = dialog.showOpenDialog(null)

            if (result != JFileChooser.APPROVE_OPTION) {
                onResult(null)
                return@launch
            }

            val file = dialog.selectedFile
            val bytes = file.readBytes()
            val mimeType = Files.probeContentType(file.toPath())
                ?: "application/octet-stream"

            onResult(FileUpload(file.name, bytes, mimeType, ProjectFileType.OTHER))
        }
    }
}