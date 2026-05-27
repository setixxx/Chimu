
package software.setixx.chimu.presentation.main

import androidx.compose.ui.graphics.vector.ImageVector
import software.setixx.chimu.api.domain.UserRole
import software.setixx.chimu.domain.model.GameJam
import software.setixx.chimu.domain.model.JamTransfer
import software.setixx.chimu.domain.model.Project
import software.setixx.chimu.domain.model.Team
import software.setixx.chimu.domain.model.UserProfile

data class MainState(
    val user: UserProfile? = null,
    val activeJams: List<GameJam> = emptyList(),
    val judgingJams: List<GameJam> = emptyList(),
    val userTeams: List<Team> = emptyList(),
    val userProjects: List<Project> = emptyList(),
    val notifications: List<Notification> = emptyList(),
    val notificationCount: Int = 0,
    val pendingTransferToReview: JamTransfer? = null,
    val isReviewActionLoading: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
){
    val canCreateJam: Boolean
        get() = user?.role == UserRole.ADMIN || user?.role == UserRole.ORGANIZER
}

enum class NotificationActionType { JAM_TRANSFER_RECEIVED }

data class Notification(
    val id: String,
    val message: String,
    val icon: ImageVector,
    val actionType: NotificationActionType? = null,
    val transferId: String? = null
)
