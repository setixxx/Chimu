package software.setixx.chimu.presentation.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import software.setixx.chimu.presentation.auth.login.LoginScreen
import software.setixx.chimu.presentation.auth.register.RegisterScreen
import software.setixx.chimu.presentation.jam.create.CreateJamScreen
import software.setixx.chimu.presentation.jam.details.JamDetailsScreen
import software.setixx.chimu.presentation.jam.edit.EditJamScreen
import software.setixx.chimu.presentation.main.MainScreen
import software.setixx.chimu.presentation.profile.ProfileScreen
import software.setixx.chimu.presentation.splash.SplashScreen
import software.setixx.chimu.presentation.team.create.CreateTeamScreen
import software.setixx.chimu.presentation.team.join.JoinTeamScreen
import software.setixx.chimu.presentation.team.details.TeamDetailsScreen
import software.setixx.chimu.presentation.user.UserProfileScreen

@Composable
fun AppNavigation(
    onNavHostReady: suspend (NavController) -> Unit = {}
) {
    val navController = rememberNavController()

    LaunchedEffect(navController) {
        onNavHostReady(navController)
    }

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
                }
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
                }
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
                },
                onNavigateToCreateJam = {
                    navController.navigate(Screen.CreateJam)
                },
                onNavigateToJamDetails = { jamId ->
                    navController.navigate(Screen.JamDetails(jamId))
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
                },
                onNavigateToUserProfile = { userId ->
                    navController.navigate(Screen.UserProfile(userId))
                }
            )
        }

        composable<Screen.UserProfile> {
            val args = it.toRoute<Screen.UserProfile>()
            UserProfileScreen(
                userId = args.userId,
                onBack = { navController.popBackStack() }
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

        composable<Screen.CreateJam> {
            CreateJamScreen(
                onBack = {
                    navController.popBackStack()
                },
                onJamCreated = { jamId ->
                    navController.navigate(Screen.JamManagement(jamId)) {
                        popUpTo(Screen.CreateJam) { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.JamDetails> {
            val args = it.toRoute<Screen.JamDetails>()
            JamDetailsScreen(
                jamId = args.jamId,
                onBack = {
                    navController.popBackStack()
                },
                onEditJam = { jamId ->
                    navController.navigate(Screen.EditJam(jamId))
                }
            )
        }

        composable<Screen.JamRegistration> {
            val args = it.toRoute<Screen.JamRegistration>()
            JamDetailsScreen(
                jamId = args.jamId,
                onBack = {
                    navController.popBackStack()
                },
                onEditJam = { jamId ->
                    navController.navigate(Screen.EditJam(jamId))
                }
            )
        }

        composable<Screen.JamProgress> {
            val args = it.toRoute<Screen.JamProgress>()
            JamDetailsScreen(
                jamId = args.jamId,
                onBack = {
                    navController.popBackStack()
                },
                onEditJam = { jamId ->
                    navController.navigate(Screen.EditJam(jamId))
                }
            )
        }

        composable<Screen.JamJudging> {
            val args = it.toRoute<Screen.JamJudging>()
            JamDetailsScreen(
                jamId = args.jamId,
                onBack = {
                    navController.popBackStack()
                },
                onEditJam = { jamId ->
                    navController.navigate(Screen.EditJam(jamId))
                }
            )
        }

        composable<Screen.JamManagement> {
            val args = it.toRoute<Screen.JamManagement>()
            JamDetailsScreen(
                jamId = args.jamId,
                onBack = {
                    navController.popBackStack()
                },
                onEditJam = { jamId ->
                    navController.navigate(Screen.EditJam(jamId))
                }
            )
        }

        composable<Screen.EditJam> {
            val args = it.toRoute<Screen.EditJam>()
            EditJamScreen(
                jamId = args.jamId,
                onBack = {
                    navController.popBackStack()
                },
                onSuccess = {
                    navController.popBackStack()
                }
            )
        }
    }
}