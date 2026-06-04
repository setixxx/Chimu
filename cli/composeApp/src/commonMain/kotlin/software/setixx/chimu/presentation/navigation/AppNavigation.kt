package software.setixx.chimu.presentation.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import software.setixx.chimu.api.domain.UserRole
import software.setixx.chimu.presentation.auth.login.LoginScreen
import software.setixx.chimu.presentation.auth.register.RegisterScreen
import software.setixx.chimu.presentation.jam.create.CreateJamScreen
import software.setixx.chimu.presentation.jam.details.JamDetailsScreen
import software.setixx.chimu.presentation.jam.details.JamDetailsTab
import software.setixx.chimu.presentation.jam.details.judging.ProjectRatingScreen
import software.setixx.chimu.presentation.jam.edit.EditJamScreen
import software.setixx.chimu.presentation.main.MainScreen
import software.setixx.chimu.presentation.profile.own.ProfileScreen
import software.setixx.chimu.presentation.splash.SplashScreen
import software.setixx.chimu.presentation.team.create.CreateTeamScreen
import software.setixx.chimu.presentation.team.join.JoinTeamScreen
import software.setixx.chimu.presentation.team.details.TeamDetailsScreen
import software.setixx.chimu.presentation.profile.alien.UserProfileScreen
import software.setixx.chimu.presentation.project.ProjectDetailsScreen

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
                    navController.navigate(Screen.OwnProfile)
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
                },
                onNavigateToJamJudging = { jamId ->
                    navController.navigate(Screen.JamDetails(jamId, JamDetailsTab.Judging.name))
                },
                onNavigateToProject = { projectId, roleStr, isAdmin ->
                   navController.navigate(Screen.ProjectDetails(projectId, roleStr, isAdmin))
                },
            )
        }

        composable<Screen.OwnProfile> {
            ProfileScreen(
                onBack = {
                    navController.popBackStack()
                },
                onDeleteAccount = {
                    navController.navigate(Screen.Login) {
                        popUpTo(Screen.Home) { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.CreateTeam> {
            CreateTeamScreen(
                onBack = {
                    navController.popBackStack()
                },
                onTeamCreated = { teamId ->
                    navController.navigate(Screen.TeamDetails(teamId)){
                        popUpTo(Screen.CreateTeam){ inclusive = true }
                    }
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
                onNavigateToAlienProfile = { userId ->
                    navController.navigate(Screen.AlienProfile(userId))
                },
                onNavigateToOwnProfile = {
                    navController.navigate(Screen.OwnProfile)
                }
            )
        }

        composable<Screen.AlienProfile> {
            val args = it.toRoute<Screen.AlienProfile>()
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
                    navController.navigate(Screen.JamDetails(jamId, JamDetailsTab.Management.name)) {
                        popUpTo(Screen.CreateJam) { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.JamDetails> {
            val args = it.toRoute<Screen.JamDetails>()

            val parsedTab = args.initialTab?.let { tabName ->
                runCatching { enumValueOf<JamDetailsTab>(tabName) }.getOrNull()
            }

            JamDetailsScreen(
                jamId = args.jamId,
                onBack = {
                    navController.popBackStack()
                },
                onEditJam = { jamId ->
                    navController.navigate(Screen.EditJam(jamId))
                },
                initialTab = parsedTab,
                onNavigateToProject = { projectId, roleStr, isAdmin ->
                    navController.navigate(Screen.ProjectDetails(projectId, roleStr, isAdmin))
                },
                onNavigateToProjectRating = { jamId, projectId ->
                    navController.navigate(Screen.ProjectRating(jamId, projectId))
                },
                onNavigateToAlienProfile = { userId ->
                    navController.navigate(Screen.AlienProfile(userId))
                },
                onNavigateToOwnProfile = {
                    navController.navigate(Screen.OwnProfile)
                }
            )
        }

        composable<Screen.ProjectRating> {
            val args = it.toRoute<Screen.ProjectRating>()
            ProjectRatingScreen(
                jamId = args.jamId,
                projectId = args.projectId,
                onBack = { navController.popBackStack() }
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

        composable<Screen.ProjectDetails> {
            val args = it.toRoute<Screen.ProjectDetails>()
            val userRole = args.userRole?.let { r ->
            runCatching { UserRole.valueOf(r) }.getOrNull()
            }
            ProjectDetailsScreen(
                projectId = args.projectId,
                userRole = userRole,
                isAdminOrOrganizer = args.isAdminOrOrganizer,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
