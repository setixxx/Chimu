package software.setixx.chimu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.koin.android.ext.koin.androidContext
import org.koin.compose.KoinApplication
import software.setixx.chimu.di.appModule
import software.setixx.chimu.di.platformModule
import software.setixx.chimu.presentation.navigation.AppNavigation
import software.setixx.chimu.presentation.theme.ChimuTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            KoinApplication(
                application = {
                    androidContext(this@MainActivity)
                    modules(platformModule(), appModule)
                }
            ) {
                ChimuTheme(darkTheme = true) {
                    AppNavigation()
                }
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    ChimuTheme {
        AppNavigation()
    }
}