package software.setixx.chimu.presentation.navigation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import software.setixx.chimu.presentation.jam.details.JamDetailsTab
import software.setixx.chimu.presentation.main.NavigationDestination

sealed interface Screen {
    @Serializable @SerialName("splash") data object Splash : Screen
    @Serializable @SerialName("login") data object Login : Screen
    @Serializable @SerialName("register") data object Register : Screen
    @Serializable @SerialName("home") data object Home : Screen
    @Serializable @SerialName("profile") data object Profile : Screen
    @Serializable @SerialName("create-team") data object CreateTeam : Screen
    @Serializable @SerialName("create-jam") data object CreateJam : Screen
    @Serializable @SerialName("join-team") data object JoinTeam : Screen
    @Serializable @SerialName("jam") data class JamDetails(val jamId: String, val initialTab: String? = null) : Screen
    @Serializable @SerialName("edit-jam") data class EditJam(val jamId: String) : Screen
    @Serializable @SerialName("team") data class TeamDetails(val teamId: String) : Screen
    @Serializable @SerialName("user-profile") data class UserProfile(val userId: String) : Screen

}
