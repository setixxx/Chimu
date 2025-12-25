package software.setixx.chimu.presentation.navigation

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import software.setixx.chimu.getPlatform
import software.setixx.chimu.presentation.auth.login.LoginScreen
import software.setixx.chimu.presentation.auth.register.RegisterScreen
import software.setixx.chimu.presentation.main.MainScreen
import software.setixx.chimu.presentation.profile.ProfileScreen
import software.setixx.chimu.presentation.team.CreateTeamScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Login
    ) {
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
            MainScreen(
                onLogout = {
                    navController.navigate(Screen.Login) {
                        popUpTo(Screen.Home) { inclusive = true }
                    }
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile)
                },
                onNavigateToCreateTeam = {
                    navController.navigate(Screen.CreateTeam)
                }
            )
        }

        composable<Screen.Profile> {
            ProfileScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<Screen.CreateTeam> {
            CreateTeamScreen(
                onBack = {
                    navController.popBackStack()
                },
                onTeamCreated = {
                    navController.popBackStack()
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