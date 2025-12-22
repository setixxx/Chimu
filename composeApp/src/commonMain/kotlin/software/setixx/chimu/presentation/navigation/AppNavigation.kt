package software.setixx.chimu.presentation.navigation

import androidx.annotation.FloatRange
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import software.setixx.chimu.getPlatform
import software.setixx.chimu.presentation.auth.login.LoginScreen
import software.setixx.chimu.presentation.auth.register.RegisterScreen
import software.setixx.chimu.presentation.home.HomeScreen
import software.setixx.chimu.presentation.splash.SplashScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash
    ) {
        composable<Screen.Splash> {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login) {
                        popUpTo(Screen.Splash) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home) {
                        popUpTo(Screen.Splash) { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.Login> {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Home) {
                        popUpTo(Screen.Login) { inclusive = true }
                    }
                },
                platformSizeModifier = calculatePlatformSizeModifier()
            )
        }

        composable<Screen.Register> {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate(Screen.Login) {
                        popUpTo(Screen.Register) { inclusive = true }
                    }
                },
                platformSizeModifier = calculatePlatformSizeModifier()
            )
        }

        composable<Screen.Home> {
            HomeScreen(
                onLogout = {
                    navController.navigate(Screen.Login) {
                        popUpTo(Screen.Home) { inclusive = true }
                    }
                }
            )
        }
    }
}

fun calculatePlatformSizeModifier() : Float {
    return if (getPlatform().name == "Android" || getPlatform().name == "iOS") {
        0.35f
    } else
        0f
}