package software.setixx.chimu.presentation.navigation

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import software.setixx.chimu.getPlatform
import software.setixx.chimu.presentation.auth.login.LoginScreen
import software.setixx.chimu.presentation.auth.register.RegisterScreen
import software.setixx.chimu.presentation.main.MainScreen
import software.setixx.chimu.presentation.profile.ProfileScreen
import software.setixx.chimu.presentation.splash.SplashScreen
import software.setixx.chimu.presentation.team.create.CreateTeamScreen
import software.setixx.chimu.presentation.team.JoinTeamScreen
import software.setixx.chimu.presentation.team.TeamDetailsScreen

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
                },
                onNavigateToTeam = { teamId ->
                    navController.navigate(Screen.TeamDetails(teamId))
                },
                onNavigateToJoinTeam = {
                    navController.navigate(Screen.JoinTeam)
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

        composable<Screen.TeamDetails> {
            val args = it.toRoute<Screen.TeamDetails>()
            TeamDetailsScreen(
                teamId = args.teamId,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<Screen.JoinTeam> {
            JoinTeamScreen(
                onBack = {
                    navController.popBackStack()
                },
                onTeamJoined = { teamId ->
                    navController.navigate(Screen.TeamDetails(teamId)) {
                        popUpTo(Screen.JoinTeam) { inclusive = true }
                    }
                }
            )
        }
    }
}

fun calculatePlatformSizeModifier(): Float {
    return if (getPlatform().name == "Android" || getPlatform().name == "iOS") {
        0.35f
    } else
        0f
}