package software.setixx.chimu.data.picker

import androidx.compose.runtime.Composable
import kotlinx.browser.document
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.w3c.dom.HTMLInputElement
import org.w3c.files.FileReader
import software.setixx.chimu.api.domain.ProjectFileType
import software.setixx.chimu.domain.model.FileUpload

@Composable
actual fun rememberFilePicker(onResult: (FileUpload?) -> Unit): () -> Unit {
    return {
        val input = document.createElement("input") as HTMLInputElement
        input.type = "file"
        input.accept = "*/*"

        input.onchange = {
            val file = input.files?.item(0)
            if (file == null) {
                onResult(null)
            } else {
                val reader = FileReader()
                reader.onload = {
                    val buffer = reader.result as ArrayBuffer
                    val int8 = Int8Array(buffer)
                    val bytes = ByteArray(int8.length) { i ->
                        (int8.asDynamic()[i] as Int).toByte()
                    }
                    onResult(FileUpload(file.name, bytes, file.type, ProjectFileType.OTHER))
                }
                reader.onerror = { onResult(null) }
                reader.readAsArrayBuffer(file)
            }
            Unit
        }

        // Добавляем в DOM чтобы браузер не блокировал клик
        document.body?.appendChild(input)
        input.click()
        document.body?.removeChild(input)
    }
}