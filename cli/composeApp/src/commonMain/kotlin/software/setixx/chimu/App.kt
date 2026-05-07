package software.setixx.chimu

import androidx.compose.runtime.*
import androidx.navigation.NavController
import org.koin.compose.KoinApplication
import software.setixx.chimu.di.appModule
import software.setixx.chimu.di.platformModule
import software.setixx.chimu.presentation.navigation.AppNavigation
import software.setixx.chimu.presentation.theme.ChimuTheme

@Composable
fun App(onNavHostReady: suspend (NavController) -> Unit = {}) {
    KoinApplication(
        application = {
            modules(platformModule(), appModule)
        }
    ) {
        ChimuTheme(darkTheme = true) {
            AppNavigation(onNavHostReady = onNavHostReady)
        }
    }
}