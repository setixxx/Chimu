package software.setixx.chimu.presentation.jam.details.project

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel
import software.setixx.chimu.api.domain.UserRole
import software.setixx.chimu.domain.model.GameJamDetails
import software.setixx.chimu.presentation.project.ProjectDetailsContent

@Composable
fun ProjectScreen(
    jamId: String,
    jam: GameJamDetails,
    userRole: UserRole?,
    userId: String?,
    onNavigateToProject: (projectId: String, roleStr: String?, isAdminOrOrganizer: Boolean) -> Unit,
    viewModel: ProjectViewModel = koinViewModel(),
    paddingValues: PaddingValues
) {
    ProjectDetailsContent(
        jamId = jamId,
        jam = jam,
        userRole = userRole,
        userId = userId,
        onNavigateToProject = onNavigateToProject,
        viewModel = viewModel,
        paddingValues = paddingValues
    )
}