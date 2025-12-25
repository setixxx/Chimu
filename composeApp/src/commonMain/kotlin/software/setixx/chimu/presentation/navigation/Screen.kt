package software.setixx.chimu.presentation.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data object Splash : Screen

    @Serializable
    data object Login : Screen

    @Serializable
    data object Register : Screen

    @Serializable
    data object Home : Screen

    @Serializable
    data object Profile : Screen

    @Serializable
    data object CreateTeam : Screen

    @Serializable
    data class TeamDetails(val teamId: String) : Screen

    @Serializable
    data object JoinTeam : Screen
}