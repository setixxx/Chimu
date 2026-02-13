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
    data object CreateJam : Screen

    @Serializable
    data class JamDetails(val jamId: String) : Screen

    @Serializable
    data class EditJam(val jamId: String) : Screen

    @Serializable
    data class TeamDetails(val teamId: String) : Screen

    @Serializable
    data object JoinTeam : Screen
}