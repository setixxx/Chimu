package software.setixx.chimu

class IosPlatform: Platform {
    override val name: String = "Ios"
}

actual fun getPlatform(): Platform = IosPlatform()