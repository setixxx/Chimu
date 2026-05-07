package software.setixx.chimu.data.picker

import androidx.compose.runtime.Composable
import software.setixx.chimu.api.domain.ProjectFileType
import software.setixx.chimu.domain.model.FileUpload
import software.setixx.chimu.domain.model.ProjectFile

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("""
    (callback) => {
        const input = document.createElement('input');
        input.type = 'file';
        input.style.display = 'none';
        document.body.appendChild(input);

        input.onchange = () => {
            const file = input.files[0];
            document.body.removeChild(input);
            if (!file) { callback(null, null, null); return; }
            const reader = new FileReader();
            reader.onload = (e) => {
                const bytes = Array.from(new Int8Array(e.target.result));
                callback(bytes, file.name, file.type);
            };
            reader.onerror = () => callback(null, null, null);
            reader.readAsArrayBuffer(file);
        };

        input.click();
    }
""")
private external fun jsOpenFilePicker(
    callback: (bytes: JsAny?, name: JsAny?, mime: JsAny?) -> Unit
)

@OptIn(ExperimentalWasmJsInterop::class)
@Composable
actual fun rememberFilePicker(onResult: (FileUpload?) -> Unit): () -> Unit {
    return {
        jsOpenFilePicker { bytesJs, nameJs, mimeJs ->
            if (bytesJs == null || nameJs == null || mimeJs == null) {
                onResult(null)
                return@jsOpenFilePicker
            }

            val name = (nameJs as JsString).toString()
            val mime = (mimeJs as JsString).toString()

            // JsArray<JsNumber> конвертируем в ByteArray
            @Suppress("UNCHECKED_CAST")
            val jsArray = bytesJs as JsArray<JsNumber>
            val bytes = ByteArray(jsArray.length) { i ->
                jsArray[i]?.toInt()?.toByte() ?: 0
            }

            onResult(FileUpload(name, bytes, mime, ProjectFileType.OTHER))
        }
    }
}