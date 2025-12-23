package software.setixx.chimu

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform