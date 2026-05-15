package software.setixx.chimu.data.util

import kotlinx.browser.window

actual fun getBaseUrl(): String {
    val hostname = window.location.hostname
    return if (hostname == "localhost" || hostname == "127.0.0.1") {
        "http://localhost:8080"
    } else {
        window.location.origin
    }
}