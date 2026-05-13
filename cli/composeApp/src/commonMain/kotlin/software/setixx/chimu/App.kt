package software.setixx.chimu

import androidx.compose.runtime.*
import androidx.navigation.NavController
import org.koin.compose.KoinApplication
import org.koin.core.KoinApplication
import org.koin.dsl.koinConfiguration
import software.setixx.chimu.di.appModule
import software.setixx.chimu.di.platformModule
import software.setixx.chimu.presentation.navigation.AppNavigation
import software.setixx.chimu.presentation.theme.ChimuTheme

@Composable
fun App(onNavHostReady: suspend (NavController) -> Unit = {}) {
    KoinApplication(
        configuration = koinConfiguration(
            declaration = {
                modules(
                    platformModule(),
                    appModule
                )
            }
        ),
        content = {
            ChimuTheme() {
                AppNavigation(onNavHostReady = onNavHostReady)
            }
        }
    )
}