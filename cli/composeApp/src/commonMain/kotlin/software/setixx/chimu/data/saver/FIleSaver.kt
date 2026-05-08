package software.setixx.chimu.data.saver

import androidx.compose.runtime.Composable

@Composable
expect fun rememberFileSaver(): (fileName: String, bytes: ByteArray, mimeType: String) -> Unit