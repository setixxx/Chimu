package software.setixx.chimu.presentation.main

import androidx.compose.ui.graphics.vector.ImageVector
import software.setixx.chimu.domain.model.User

data class MainState(
    val user: User? = null,
    val activeJams: List<GameJamPreview> = emptyList(),
    val notifications: List<Notification> = emptyList(),
    val notificationCount: Int = 0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

data class GameJamPreview(
    val id: String,
    val name: String,
    val theme: String?,
    val status: String,
    val teamsCount: Int,
    val daysRemaining: Int?
)

data class Notification(
    val id: String,
    val message: String,
    val icon: ImageVector
)