package software.setixx.chimu.data.downloader

import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("""
    (base64, fileName, mimeType) => {
        const binary = atob(base64);
        const bytes = new Uint8Array(binary.length);
        for (let i = 0; i < binary.length; i++) bytes[i] = binary.charCodeAt(i);
        const blob = new Blob([bytes], { type: mimeType });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = fileName;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
    }
""")
private external fun jsSaveBase64(base64: String, fileName: String, mimeType: String)

actual class FileDownloader {
    @OptIn(ExperimentalWasmJsInterop::class, ExperimentalEncodingApi::class)
    actual suspend fun save(fileName: String, bytes: ByteArray, mimeType: String) {
        val base64 = kotlin.io.encoding.Base64.encode(bytes)
        jsSaveBase64(base64, fileName, mimeType)
    }
}