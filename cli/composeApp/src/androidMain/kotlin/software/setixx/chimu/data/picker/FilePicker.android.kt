package software.setixx.chimu.data.picker

import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import software.setixx.chimu.api.domain.ProjectFileType
import software.setixx.chimu.domain.model.FileUpload
import software.setixx.chimu.domain.model.ProjectFile

@Composable
actual fun rememberFilePicker(onResult: (FileUpload?) -> Unit): () -> Unit {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri == null) { onResult(null); return@rememberLauncherForActivityResult }

        val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
        val fileName = context.contentResolver
            .query(uri, null, null, null, null)
            ?.use { cursor ->
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                cursor.getString(index)
            } ?: "file"

        val bytes = context.contentResolver
            .openInputStream(uri)
            ?.use { it.readBytes() }

        if (bytes == null) { onResult(null); return@rememberLauncherForActivityResult }

        onResult(FileUpload(fileName, bytes, mimeType, ProjectFileType.OTHER))
    }

    return { launcher.launch("*/*") }
}