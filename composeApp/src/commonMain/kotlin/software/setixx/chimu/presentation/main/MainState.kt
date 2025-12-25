
package software.setixx.chimu.presentation.main

import androidx.compose.ui.graphics.vector.ImageVector
import software.setixx.chimu.domain.model.GameJam
import software.setixx.chimu.domain.model.Project
import software.setixx.chimu.domain.model.Team
import software.setixx.chimu.domain.model.User

data class MainState(
    val user: User? = null,
    val activeJams: List<GameJam> = emptyList(),
    val userTeams: List<Team> = emptyList(),
    val userProjects: List<Project> = emptyList(),
    val notifications: List<Notification> = emptyList(),
    val notificationCount: Int = 0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

data class Notification(
    val id: String,
    val message: String,
    val icon: ImageVector
)