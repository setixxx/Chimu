package software.setixx.chimu.presentation.jam.details.judging.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import software.setixx.chimu.api.domain.UserRole
import software.setixx.chimu.domain.model.Project
import software.setixx.chimu.presentation.jam.details.judging.JudgingState

@Composable
fun JudgeView(
    state: JudgingState,
    onSelectProject: (Project) -> Unit,
    onNavigateToProject: ((projectId: String, roleStr: String?, isAdminOrOrganizer: Boolean) -> Unit)? = null
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        state.judgeProgress?.let { progress ->
            JudgeProgressCard(
                ratedProjects = progress.ratedProjects,
                totalProjects = progress.totalProjects
            )
        }

        state.judgeProgress?.let { progress ->
            if (progress.missingProjects.isNotEmpty()) {
                MissingRatingsCard(missingCount = progress.missingProjects.size)
            }
        }

        Text(
            text = "Проекты для оценки (${state.projects.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        if (state.projects.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Inbox,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Нет проектов для оценки",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            val ratedProjectIds = state.judgeProgress?.missingProjects
                ?.map { it.projectId }?.toSet() ?: emptySet()

            state.projects.forEach { project ->
                val isMissingRating = ratedProjectIds.contains(project.id)
                ProjectCard(
                    project = project,
                    hasUnratedCriteria = isMissingRating,
                    onClick = { onSelectProject(project) },
                    onViewDetails = onNavigateToProject?.let {
                        { it(project.id, UserRole.JUDGE.name, false) }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}
